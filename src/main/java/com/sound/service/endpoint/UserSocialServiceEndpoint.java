package com.sound.service.endpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.constant.Constant;
import com.sound.exception.SoundException;
import com.sound.exception.UserException;
import com.sound.model.User;
import com.sound.service.user.itf.UserSocialService;
import com.sound.util.JsonHandler;

@Component
@Path("/userActivity")
@RolesAllowed({Constant.ADMIN_ROLE, Constant.USER_ROLE})
public class UserSocialServiceEndpoint {

  Logger logger = Logger.getLogger(UserSocialServiceEndpoint.class);

  @Autowired
  UserSocialService userSocialService;

  @Autowired
  com.sound.service.user.itf.UserService userService;

  @Context
  HttpServletRequest req;

  @PUT
  @Path("/follow/{toUserAlias}")
  public Response follow(@NotNull @PathParam("toUserAlias") String toUserAlias) {
    Long followed = 0L;
    User fromUser = null;
    User toUser = null;
    try {
      fromUser = userService.getCurrentUser(req);
      
      if (!fromUser.getProfile().getAlias().equals(toUserAlias))
      {
        toUser = userService.getUserByAlias(toUserAlias);
        followed = userSocialService.follow(fromUser, toUser);
      }
    } catch (UserException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to follow user " + toUserAlias)).build();
    }

    Map<String, Long> result = new HashMap<String, Long>();
    result.put("followed", followed);
    return Response.status(Status.OK).entity(JsonHandler.toJson(result)).build();
  }

  @DELETE
  @Path("/follow/{toUserAlias}")
  public Response unfollow(@NotNull @PathParam("toUserAlias") String toUserAlias) {
    Long followed = 0L;
    User fromUser = null;
    User toUser = null;
    try {
      fromUser = userService.getCurrentUser(req);
      toUser = userService.getUserByAlias(toUserAlias);
      followed = userSocialService.unfollow(fromUser, toUser);
    } catch (UserException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to follow user " + toUserAlias)).build();
    }
    Map<String, Long> result = new HashMap<String, Long>();
    result.put("followed", followed);
    return Response.status(Status.OK).entity(JsonHandler.toJson(result)).build();
  }

  @PUT
  @Path("/group/{groupName}")
  public Response createGroup(@NotNull @PathParam("groupName") String groupName,
      @NotNull @FormParam("description") String description) {
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      userSocialService.createGroup(currentUser, groupName, description);
    } catch (UserException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to create group " + groupName)).build();
    }
    return Response.status(Status.OK).entity("true").build();
  }

  @DELETE
  @Path("/group/{groupName}")
  public Response dismissGroup(@NotNull @PathParam("groupName") String groupName) {
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      userSocialService.dismissGroup(currentUser, groupName);
    } catch (UserException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to delete group " + groupName)).build();
    }
    return Response.status(Status.OK).entity("true").build();
  }

  @PUT
  @Path("/joinGroup/{groupName}")
  public Response joinGroup(@NotNull @PathParam("groupName") String groupName) {
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      userSocialService.joinGroup(currentUser, groupName);
    } catch (UserException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to join group " + groupName)).build();
    }
    return Response.status(Status.OK).entity("true").build();
  }

  @DELETE
  @Path("/joinGroup/{groupName}")
  public Response leaveGroup(@NotNull @PathParam("userAlias") String userAlias,
      @NotNull @PathParam("groupName") String groupName) {
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      userSocialService.leaveGroup(currentUser, groupName);
    } catch (UserException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to leave group " + groupName)).build();
    }
    return Response.status(Status.OK).entity("true").build();
  }

  @PUT
  @Path("/{groupName}/promoteAdmin/{adminAlias}")
  public Response promoteGroupAdmin(@NotNull @PathParam("userAlias") String adminAlias,
      @NotNull @PathParam("groupName") String groupName) {
    User currentUser = null;
    User adminUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      adminUser = userService.getUserByAlias(adminAlias);
      userSocialService.promoteGroupAdmin(currentUser, adminUser, groupName);
    } catch (UserException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to promote group admin" + groupName)).build();
    }
    return Response.status(Status.OK).entity("true").build();
  }

  @DELETE
  @Path("/{groupName}/promoteAdmin/{adminAlias}")
  public Response demoteGroupAdmin(@NotNull @PathParam("userAlias") String adminAlias,
      @NotNull @PathParam("groupName") String groupName) {
    User currentUser = null;
    User adminUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      adminUser = userService.getUserByAlias(adminAlias);
      userSocialService.promoteGroupAdmin(currentUser, adminUser, groupName);
    } catch (UserException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to demote group admin" + groupName)).build();
    }
    return Response.status(Status.OK).entity("true").build();
  }

  @POST
  @Path("/recommand/users")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response getRecommandedUsersByTags(@NotNull JSONObject inputJsonObj) {
    List<User> users = new ArrayList<User>();
    User currentUser = userService.getCurrentUser(req);
    try {
      Integer pageNum = inputJsonObj.getInt("pageNum");
      Integer pageSize = inputJsonObj.getInt("pageSize");

      List<String> tagList = new ArrayList<String>();
      int len = inputJsonObj.getJSONArray("tags").length();
      for (int i = 0; i < len; i++) {
        tagList.add(inputJsonObj.getJSONArray("tags").get(i).toString());
      }
      users.addAll(userSocialService.recommandUsersByTags(currentUser, tagList, pageNum, pageSize));
    } catch (UserException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return Response.status(Status.OK).entity(users).build();
  }

  @POST
  @Path("/recommand/user")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response getRecommandedUsersForUser(@NotNull JSONObject inputJsonObj) {
    List<User> users = new ArrayList<User>();
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      Integer pageNum = inputJsonObj.getInt("pageNum");
      Integer pageSize = inputJsonObj.getInt("pageSize");

      users.addAll(userSocialService.recommandUsersForUser(currentUser, pageNum, pageSize));
    } catch (UserException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return Response.status(Status.OK).entity(users).build();
  }

  @GET
  @Path("/followed")
  public Response getFollowedUsers(@NotNull @QueryParam("pageNum") Integer pageNum,
      @NotNull @QueryParam("pageSize") Integer pageSize) {
    List<User> users = new ArrayList<User>();
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      users.addAll(userSocialService.getFollowedUsers(currentUser, pageNum, pageSize));
    } catch (UserException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
    return Response.status(Status.OK).entity(users).build();
  }

  @GET
  @Path("/following")
  public Response getFollowingUsers(@NotNull @QueryParam("pageNum") Integer pageNum,
      @NotNull @QueryParam("pageSize") Integer pageSize) {
    List<User> users = new ArrayList<User>();
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      users.addAll(userSocialService.getFollowingUsers(currentUser, pageNum, pageSize));
    } catch (UserException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
    return Response.status(Status.OK).entity(users).build();
  }
}

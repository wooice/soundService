package com.sound.service.endpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.json.JsonException;
import javax.json.JsonObject;
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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.constant.Constant;
import com.sound.exception.SoundException;
import com.sound.exception.UserException;
import com.sound.model.User;
import com.sound.service.user.itf.UserSocialService;

@Component
@Path("/userActivity")
@RolesAllowed({Constant.ADMIN_ROLE, Constant.USER_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE})
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
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Long> follow(@NotNull @PathParam("toUserAlias") String toUserAlias) {
    Long followed = 0L;
    User fromUser = null;
    User toUser = null;
    try {
      fromUser = userService.getCurrentUser(req);

      if (!fromUser.getProfile().getAlias().equals(toUserAlias)) {
        toUser = userService.getUserByAlias(toUserAlias);
        followed = userSocialService.follow(fromUser, toUser);
      }
    } catch (UserException e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    Map<String, Long> result = new HashMap<String, Long>();
    result.put("followed", followed);
    return result;
  }

  @DELETE
  @Path("/follow/{toUserAlias}")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Long> unfollow(@NotNull @PathParam("toUserAlias") String toUserAlias) {
    Long followed = 0L;
    User fromUser = null;
    User toUser = null;
    try {
      fromUser = userService.getCurrentUser(req);
      toUser = userService.getUserByAlias(toUserAlias);
      followed = userSocialService.unfollow(fromUser, toUser);
    } catch (UserException e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    Map<String, Long> result = new HashMap<String, Long>();
    result.put("followed", followed);
    return result;
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
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
    return Response.status(Status.OK).build();
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
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
    return Response.status(Status.OK).build();
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
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
    return Response.status(Status.OK).build();
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
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
    return Response.status(Status.OK).build();
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
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
    return Response.status(Status.OK).build();
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
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
    return Response.status(Status.OK).build();
  }

  @POST
  @Path("/recommand/users")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public List<User> getRecommandedUsersByTags(@NotNull JsonObject inputJsonObj) {
    List<User> users = new ArrayList<User>();
    User currentUser = userService.getCurrentUser(req);
    try {
      Integer pageNum = inputJsonObj.getInt("pageNum");
      Integer pageSize = inputJsonObj.getInt("pageSize");

      List<String> tagList = new ArrayList<String>();
      int len = inputJsonObj.getJsonArray("tags").size();
      for (int i = 0; i < len; i++) {
        tagList.add(inputJsonObj.getJsonArray("tags").get(i).toString());
      }
      users.addAll(userSocialService.recommandUsersByTags(currentUser, tagList, pageNum, pageSize));
    } catch (UserException e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (SoundException e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (JsonException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return users;
  }

  @POST
  @Path("/recommand/user")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public List<User> getRecommandedUsersForUser(@NotNull JsonObject inputJsonObj) {
    List<User> users = new ArrayList<User>();
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      Integer pageNum = inputJsonObj.getInt("pageNum");
      Integer pageSize = inputJsonObj.getInt("pageSize");

      if (null != currentUser) {
        users.addAll(userSocialService.recommandUsersForUser(currentUser, pageNum, pageSize));
      }
    } catch (UserException e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (SoundException e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (JsonException e) {
      e.printStackTrace();
    }
    return users;
  }

  @GET
  @Path("/{userAlias}/followed")
  @Produces(MediaType.APPLICATION_JSON)
  public List<User> getFollowedUsers(@NotNull @PathParam("userAlias") String userAlias,
      @NotNull @QueryParam("pageNum") Integer pageNum,
      @NotNull @QueryParam("pageSize") Integer pageSize) {
    List<User> users = new ArrayList<User>();
    User user = null;
    try {
      user = userService.getUserByAlias(userAlias);
      users.addAll(userSocialService.getFollowedUsers(user, pageNum, pageSize));
    } catch (UserException e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    return users;
  }

  @GET
  @Path("/{userAlias}/following")
  @Produces(MediaType.APPLICATION_JSON)
  public List<User> getFollowingUsers(@NotNull @PathParam("userAlias") String userAlias,
      @NotNull @QueryParam("pageNum") Integer pageNum,
      @NotNull @QueryParam("pageSize") Integer pageSize) {
    List<User> users = new ArrayList<User>();
    User user = null;
    try {
      user = userService.getUserByAlias(userAlias);
      users.addAll(userSocialService.getFollowingUsers(user, pageNum, pageSize));
    } catch (UserException e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    return users;
  }
}

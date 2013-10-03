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
import com.sound.model.Sound;
import com.sound.model.SoundActivity.SoundComment;
import com.sound.model.User;
import com.sound.service.sound.itf.SoundSocialService;
import com.sound.util.JsonHandler;

@Component
@Path("/soundActivity")
@RolesAllowed({Constant.ADMIN_ROLE, Constant.USER_ROLE})
public class SoundSocialServiceEndpoint {

  Logger logger = Logger.getLogger(SoundSocialServiceEndpoint.class);

  @Autowired
  SoundSocialService soundSocialService;

  @Autowired
  com.sound.service.user.itf.UserService userService;

  @Context
  HttpServletRequest req;

  @PUT
  @Path("/play/{soundAlias}")
  public Response play(@NotNull @PathParam("soundAlias") String soundAlias) {
    Map<String, String> result = null;
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      result = soundSocialService.play(soundAlias, currentUser);
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
    return Response.status(Status.OK).entity(JsonHandler.toJson(result)).build();
  }

  @PUT
  @Path("/like/{soundAlias}")
  public Response like(@NotNull @PathParam("soundAlias") String soundAlias) {
    Integer liked = 0;
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      liked = soundSocialService.like(soundAlias, currentUser);
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
    Map<String, Integer> result = new HashMap<String, Integer>();
    result.put("liked", liked);
    return Response.status(Status.OK).entity(JsonHandler.toJson(result)).build();
  }

  @DELETE
  @Path("/like/{soundAlias}")
  public Response unlike(@NotNull @PathParam("soundAlias") String soundAlias) {
    Integer liked = 0;
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      liked = soundSocialService.dislike(soundAlias, currentUser);
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
    Map<String, Integer> result = new HashMap<String, Integer>();
    result.put("liked", liked);
    return Response.status(Status.OK).entity(JsonHandler.toJson(result)).build();
  }

  @PUT
  @Path("/repost/{soundAlias}")
  public Response repost(@NotNull @PathParam("soundAlias") String soundAlias) {
    Integer reposted = 0;
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      reposted = soundSocialService.repost(soundAlias, currentUser);
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
    Map<String, Integer> result = new HashMap<String, Integer>();
    result.put("reposted", reposted);
    return Response.status(Status.OK).entity(JsonHandler.toJson(result)).build();
  }

  @DELETE
  @Path("/repost/{soundAlias}")
  public Response unrepost(@NotNull @PathParam("soundAlias") String soundAlias) {
    Integer reposted = 0;
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      reposted = soundSocialService.unrepost(soundAlias, currentUser);
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
    Map<String, Integer> result = new HashMap<String, Integer>();
    result.put("reposted", reposted);
    return Response.status(Status.OK).entity(JsonHandler.toJson(result)).build();
  }

  @PUT
  @Path("/comment/{soundAlias}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response comment(@NotNull @PathParam("soundAlias") String soundAlias,
      @NotNull JSONObject inputJsonObj) {
    Integer commentsCount = 0;
    User currentUser = null;
    try {
      String comment = inputJsonObj.getString("comment");
      Float pointAt = null;

      if (null != inputJsonObj.get("pointAt")) {
        try {
          pointAt = (float) inputJsonObj.getDouble("pointAt");
        } catch (JSONException e) {}
      }
      String toUserAlias = inputJsonObj.getString("toUserAlias");
      currentUser = userService.getCurrentUser(req);
      User toUser = null;
      if (null != toUserAlias) {
        toUser = userService.getUserByAlias(toUserAlias);
      }
      commentsCount = soundSocialService.comment(soundAlias, currentUser, toUser, comment, pointAt);
    } catch (UserException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
    Map<String, Integer> result = new HashMap<String, Integer>();
    result.put("commentsCount", commentsCount);
    return Response.status(Status.OK).entity(JsonHandler.toJson(result)).build();
  }

  @DELETE
  @Path("/comment/{commentId}")
  public Response comment(@NotNull @PathParam("commentId") String commentId) {
    Integer commentsCount = 0;
    try {
      commentsCount = soundSocialService.uncomment(commentId);
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
    Map<String, Integer> result = new HashMap<String, Integer>();
    result.put("commentsCount", commentsCount);
    return Response.status(Status.OK).entity(JsonHandler.toJson(result)).build();
  }

  @GET
  @Path("/{soundAlias}/comments")
  public Response comment(@NotNull @PathParam("soundAlias") String soundAlias,
      @NotNull @QueryParam("pageNum") Integer pageNum,
      @QueryParam("commentsPerPage") Integer commentsPerPage) {
    List<SoundComment> comments = null;
    try {
      comments = soundSocialService.getComments(soundAlias, pageNum, commentsPerPage);
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson(comments)).build();
  }

  @POST
  @Path("/recommand/sounds")
  public Response getRecommandedGroupsByTags(@NotNull @FormParam("tags") List<String> tags,
      @NotNull @FormParam("pageNum") Integer pageNum,
      @NotNull @FormParam("pageSize") Integer pageSize) {
    List<Sound> sounds = new ArrayList<Sound>();
    try {
      sounds.addAll(soundSocialService.recommandSoundsByTags(tags, pageNum, pageSize));
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
    return Response.status(Status.OK).entity(JsonHandler.toJson(sounds)).build();
  }
}

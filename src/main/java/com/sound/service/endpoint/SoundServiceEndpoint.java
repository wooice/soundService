package com.sound.service.endpoint;

import java.util.List;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.constant.Constant;
import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.Sound.SoundProfile;
import com.sound.model.SoundActivity.SoundRecord;
import com.sound.model.User;
import com.sound.service.sound.itf.SoundService;
import com.sound.util.JsonHandler;

@Component
@Path("/sound")
@RolesAllowed({Constant.ADMIN_ROLE, Constant.USER_ROLE})
public class SoundServiceEndpoint {

  Logger logger = Logger.getLogger(SoundServiceEndpoint.class);

  @Autowired
  SoundService soundService;

  @Autowired
  com.sound.service.user.itf.UserService userService;

  @Context
  HttpServletRequest req;

  @GET
  @Path("/{soundAlias}")
  public Response loadSound(@NotNull @PathParam("soundAlias") String soundAlias) {
    Sound sound = null;
    User currentUser = userService.getCurrentUser(req);
    try {
      sound = soundService.load(currentUser, soundAlias);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity("Failed to load sound " + soundAlias).build();
    }

    return Response.status(Status.OK).entity(sound.toString()).build();
  }

  @PUT
  @Path("/{soundName}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response saveProfile(@NotNull @PathParam("soundName") String soundName,
      @NotNull SoundProfile soundProfile) {
    SoundProfile profile = null;
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      profile = soundService.saveProfile(soundProfile, currentUser);
    } catch (SoundException e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to save sound " + soundName)).build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson(profile)).build();
  }

  @POST
  @Path("/{soundAlias}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateProfile(@NotNull @PathParam("soundAlias") String soundAlias,
      @NotNull SoundProfile soundProfile) {
    SoundProfile profile = null;
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);

      if (soundService.isOwner(currentUser, soundAlias)) {
        soundProfile.setAlias(soundAlias);
      } else {
        return Response.status(Status.FORBIDDEN).entity("You don't have write access").build();
      }
      profile = soundService.updateProfile(soundProfile);
    } catch (SoundException e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to save sound " + soundProfile.getAlias())).build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson(profile)).build();
  }

  @PUT
  @Path("/addToSet")
  public Response addToSet(@NotNull @FormParam("userId") String userId,
      @NotNull @FormParam("soundId") String soundId, @FormParam("SetId") String setId) {
    soundService.addToSet(soundId, setId);

    return Response.status(Status.OK).entity("true").build();
  }

  @DELETE
  @Path("/{soundAlias}")
  public Response delete(@NotNull @PathParam("soundAlias") String soundAlias) {
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);

      if (soundService.isOwner(currentUser, soundAlias)) {
        soundService.delete(soundAlias);
      } else {
        return Response.status(Status.FORBIDDEN).entity("You don't have write access").build();
      }
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to delete sound " + soundAlias)).build();
    }
    return Response.status(Status.OK).entity("true").build();
  }

  @GET
  @Path("/streams/search")
  public Response listSoundsByKeyword(@NotNull @QueryParam("q") String keyword,
      @QueryParam("pageNum") Integer pageNum, @QueryParam("soundsPerPage") Integer soundsPerPage) {
    pageNum = (null == pageNum) ? 0 : pageNum;
    soundsPerPage = (null == soundsPerPage) ? 15 : soundsPerPage;

    List<Sound> sounds = null;
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      sounds = soundService.loadByKeyWords(currentUser, keyword, pageNum, soundsPerPage);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to load sound streams.")
          .build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson(sounds)).build();
  }

  @GET
  @Path("/streams/{userAlias}")
  public Response listUsersSounds(@QueryParam("pageNum") Integer pageNum,
      @PathParam("userAlias") String userAlias,
      @QueryParam("soundsPerPage") Integer soundsPerPage) {
    pageNum = (null == pageNum) ? 0 : pageNum;
    soundsPerPage = (null == soundsPerPage) ? 15 : soundsPerPage;

    List<SoundRecord> sounds = null;
    User user = null;
    try {
      user = userService.getUserByAlias(userAlias);
      sounds = soundService.getSoundsByUser(user, pageNum, soundsPerPage);
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to load sound streams.")
          .build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson(sounds)).build();
  }

  @GET
  @Path("/streams")
  public Response listObservingSounds(@QueryParam("pageNum") Integer pageNum,
      @QueryParam("soundsPerPage") Integer soundsPerPage) {
    pageNum = (null == pageNum) ? 0 : pageNum;
    soundsPerPage = (null == soundsPerPage) ? 15 : soundsPerPage;

    List<SoundRecord> sounds = null;
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      sounds = soundService.getObservingSounds(currentUser, pageNum, soundsPerPage);
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to load sounds for current user.")).build();
    }
    return Response.status(Status.OK).entity(JsonHandler.toJson(sounds)).build();
  }

  @GET
  @Path("/toupload")
  public Response getSoundToUpload() {
    Sound sound = null;
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      sound = soundService.getUnfinishedUpload(currentUser);
    } catch (Exception e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to load sounds for user")).build();
    }
    return Response.status(Status.OK).entity(JsonHandler.toJson(sound)).build();
  }

}

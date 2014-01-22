package com.sound.service.endpoint;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.constant.Constant;
import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.SoundActivity.SoundPlay;
import com.sound.model.SoundActivity.SoundVisit;
import com.sound.model.User;
import com.sound.service.sound.itf.SoundService;
import com.sound.service.sound.itf.SoundSocialService;

@Component
@Path("/soundActivityPro")
@RolesAllowed({Constant.ADMIN_ROLE, Constant.USER_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE})
public class SoundSocialProServiceEndpoint {

  Logger logger = Logger.getLogger(SoundSocialProServiceEndpoint.class);

  @Autowired
  SoundSocialService soundSocialService;

  @Autowired
  SoundService soundService;

  @Autowired
  com.sound.service.user.itf.UserService userService;
  
  @Context
  HttpServletRequest req;
  
  @GET
  @Path("/{soundId}/plays")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({Constant.ADMIN_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE})
  public List<SoundPlay> played(@NotNull @PathParam("soundId") String soundId,
      @NotNull @QueryParam("pageNum") Integer pageNum,
      @NotNull @QueryParam("perPage") Integer perPage) {
    List<SoundPlay> plays = null;
    try {
      Sound sound = soundService.loadById(soundId);
      if (null == sound) {
        throw new WebApplicationException(Status.NOT_FOUND);
      }

      plays = soundSocialService.getPlayed(sound, pageNum, perPage);
      
      User curUser = userService.getCurrentUser(req);
      for (SoundPlay play : plays) {
        play.getOwner().setUserPrefer(userService.getUserPrefer(curUser, play.getOwner()));
      }
    } catch (SoundException e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return plays;
  }
  
  
  @GET
  @Path("/{soundId}/visits")
  @Produces(MediaType.APPLICATION_JSON)
  public List<SoundVisit> visits(@NotNull @PathParam("soundId") String soundId,
      @NotNull @QueryParam("pageNum") Integer pageNum,
      @NotNull @QueryParam("perPage") Integer perPage) {
    List<SoundVisit> visits = null;
    try {
      Sound sound = soundService.loadById(soundId);
      if (null == sound) {
        throw new WebApplicationException(Status.NOT_FOUND);
      }

      visits = soundSocialService.getVisits(sound, pageNum, perPage);
 
      User curUser = userService.getCurrentUser(req);
      for (SoundVisit visit : visits) {
        visit.getOwner().setUserPrefer(userService.getUserPrefer(curUser, visit.getOwner()));
      }
    } catch (SoundException e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return visits;
  }
}

package com.sound.service.endpoint;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.constant.Constant;
import com.sound.model.Sound;
import com.sound.model.User;
import com.sound.service.sound.itf.SoundService;

@Component
@Path("/soundPro")
@RolesAllowed({Constant.ADMIN_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE})
public class SoundServiceProEndpoint {

  Logger logger = Logger.getLogger(SoundServiceProEndpoint.class);

  @Autowired
  SoundService soundService;

  @Autowired
  com.sound.service.user.itf.UserService userService;

  @Context
  HttpServletRequest req;

  @POST
  @Path("/{soundId}/promote")
  public Response promoteSound(@NotNull @PathParam("soundId") String soundId)
  {
    User curUser=userService.getCurrentUser(req);
    Sound sound = soundService.loadById(soundId);
    
    if (null == sound)
    {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    
    if (!sound.getProfile().getOwner().equals(curUser))
    {
      throw new WebApplicationException(Status.FORBIDDEN);
    }
    
    soundService.promoteSound(sound);
    
    return Response.status(Status.OK).build();
  }
  
  @POST
  @Path("/{soundId}/demote")
  public Response demoteSound(@NotNull @PathParam("soundId") String soundId)
  {
    User curUser=userService.getCurrentUser(req);
    Sound sound = soundService.loadById(soundId);
    
    if (null == sound)
    {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    
    if (!sound.getProfile().getOwner().equals(curUser))
    {
      throw new WebApplicationException(Status.FORBIDDEN);
    }
    
    soundService.demoteSound(sound);
    
    return Response.status(Status.OK).build();
  }
}

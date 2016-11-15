package com.sound.service.endpoint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import com.sound.filter.authentication.ResourceAllowed;
import com.sound.model.Sound;
import com.sound.model.Sound.SoundProfile;
import com.sound.model.Tag;
import com.sound.model.User;
import com.sound.model.enums.SoundState;
import com.sound.service.sound.itf.PlayListService;
import com.sound.service.sound.itf.SoundService;
import com.sound.service.sound.itf.SoundSocialService;
import com.sound.service.sound.itf.TagService;
import com.sound.service.storage.itf.RemoteStorageService;

@Component
@Path("/sound")
@RolesAllowed({Constant.ADMIN_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE, Constant.USER_ROLE, Constant.GUEST_ROLE})
public class SoundServiceEndpoint {

  Logger logger = Logger.getLogger(SoundServiceEndpoint.class);

  @Autowired
  SoundService soundService;
  
  @Autowired
  SoundSocialService soundSocialService;
  
  @Autowired
  TagService tagService;

  @Autowired
  com.sound.service.user.itf.UserService userService;
  
  @Autowired
  RemoteStorageService remoteStorageService;
  
  @Autowired
  PlayListService playListService;

  @Context
  HttpServletRequest req;

  @GET
  @Path("/{soundAlias}")
  @Produces(MediaType.APPLICATION_JSON)
  @ResourceAllowed
  public Sound loadSound(@NotNull @PathParam("soundAlias") String soundAlias) {
    Sound sound = null;
    User currentUser = userService.getCurrentUser(req);

    try {
      sound = soundService.load(currentUser, soundAlias);

      if (null == sound || sound.getProfile().getStatus().equals(SoundState.DELETE.getStatusName())) {
        throw new WebApplicationException(Status.NOT_FOUND);
      }
      User owner = sound.getProfile().getOwner();
      if ((null == currentUser || (null != currentUser && !currentUser.equals(owner)))
          && sound.getProfile().getStatus().equals(SoundState.PRIVATE.getStatusName())) {
        throw new WebApplicationException(Status.FORBIDDEN);
      }
      
      sound.setUserPrefer(soundService.getUserPreferOfSound(sound, currentUser));
      
      if (!sound.getProfile().getOwner().equals(currentUser))
      {
        soundSocialService.addVisit(sound, currentUser);
      }
    } catch (WebApplicationException e) {
      throw e;
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    return sound;
  }
  
  @PUT
  @Path("/{soundName}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({Constant.ADMIN_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE, Constant.USER_ROLE})
  public Sound saveProfile(@NotNull @PathParam("soundName") String soundName,
      @NotNull SoundProfile soundProfile) {
    Sound sound = null;
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      sound = soundService.saveProfile(soundProfile, currentUser);
    } catch (SoundException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return sound;
  }

  @POST
  @Path("/{soundId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({Constant.ADMIN_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE, Constant.USER_ROLE})
  public Sound updateProfile(@NotNull @PathParam("soundId") String soundId,
      @NotNull SoundProfile soundProfile) {
    Sound sound = null;
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);

      if (!soundService.isOwner(currentUser, soundId)) {
        throw new WebApplicationException(Status.FORBIDDEN);
      }
      
      if (currentUser.getUserRoles().contains(Constant.USER_ROLE_OBJ) && null != soundProfile.getCommentMode())
      {
        soundProfile.setCommentMode(Constant.COMMENT_PUBLIC);
      }
      
      sound = soundService.updateProfile(soundId, soundProfile);
    } catch (WebApplicationException e) {
      throw e;
    } catch (SoundException e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return sound;
  }

  @PUT
  @Path("/addToSet")
  @RolesAllowed({Constant.ADMIN_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE, Constant.USER_ROLE})
  public Response addToSet(@NotNull @FormParam("userId") String userId,
      @NotNull @FormParam("soundId") String soundId, @FormParam("SetId") String setId) {
    soundService.addToSet(soundId, setId);

    return Response.status(Status.OK).build();
  }
 
  @GET
  @Path("/playlist")
  @RolesAllowed({Constant.ADMIN_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE, Constant.USER_ROLE})
  public List<Sound> getPlayList() {
    User curUser = userService.getCurrentUser(req);

    return playListService.list(curUser, 0, 200);//TODO: pagination
  }
  
  @POST
  @Path("/playlist")
  @RolesAllowed({Constant.ADMIN_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE, Constant.USER_ROLE})
  public Response addToPlaylist(@NotNull @QueryParam("soundId") String soundId) {
    Sound sound = soundService.loadById(soundId);
    User curUser = userService.getCurrentUser(req);
    playListService.add(curUser, sound);
    userService.saveUser(curUser);

    return Response.status(Status.OK).build();
  }
  
  @DELETE
  @Path("/playlist")
  @RolesAllowed({Constant.ADMIN_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE, Constant.USER_ROLE})
  public Response deleteFromPlaylist(@NotNull @QueryParam("soundId") String soundId) {
    Sound sound = soundService.loadById(soundId);
    User curUser = userService.getCurrentUser(req);
    playListService.remove(curUser, sound);
    userService.saveUser(curUser);

    return Response.status(Status.OK).build();
  }

  @DELETE
  @Path("/{soundAlias}")
  @RolesAllowed({Constant.ADMIN_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE, Constant.USER_ROLE})
  public Response delete(@NotNull @PathParam("soundAlias") String soundAlias) {
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);

      if (soundService.isOwner(currentUser, soundAlias) || currentUser.getUserRoles().contains(Constant.ADMIN_ROLE_OBJ)) {
        soundService.delete(soundAlias);
      } else {
        return Response.status(Status.FORBIDDEN).build();
      }
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
    return Response.status(Status.OK).build();
  }
  
  @DELETE
  @Path("/{remoteId}/discard")
  @RolesAllowed({Constant.ADMIN_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE, Constant.USER_ROLE})
  public Response discard(@NotNull @PathParam("remoteId") String remoteId) {
    try {
      soundService.deleteByRemoteId(remoteId);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
    return Response.status(Status.OK).build();
  }

  @POST
  @Path("/streams/playlist/{listName}")
  @Produces(MediaType.APPLICATION_JSON)
  @ResourceAllowed
  public List<Sound> listSoundsInList(@NotNull @PathParam("listName") String listName,
      @QueryParam("pageNum") Integer pageNum, @QueryParam("soundsPerPage") Integer soundsPerPage) {
    pageNum = (null == pageNum) ? 0 : pageNum;
    soundsPerPage = (null == soundsPerPage) ? 15 : soundsPerPage;

    List<Sound> sounds = null;
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      sounds =  playListService.list(currentUser, pageNum, soundsPerPage);
      
      for (Sound sound: sounds)
      {
    	  soundService.buildSoundSocial(sound);
    	  sound.setUserPrefer(soundService.getUserPreferOfSound(sound, currentUser));
      }
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return sounds;
  }
  
  @POST
  @Path("/streams/match/{q}")
  @Produces(MediaType.APPLICATION_JSON)
  @ResourceAllowed
  public List<Sound> listSoundsByKeyword(@NotNull @PathParam("q") String keyword,
      @QueryParam("pageNum") Integer pageNum, @QueryParam("soundsPerPage") Integer soundsPerPage) {
    pageNum = (null == pageNum) ? 0 : pageNum;
    soundsPerPage = (null == soundsPerPage) ? 15 : soundsPerPage;

    List<Sound> sounds = null;
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      sounds = soundService.loadByKeyWords(currentUser, keyword, pageNum, soundsPerPage);
      for (Sound sound: sounds)
      {
    	  sound.setUserPrefer(soundService.getUserPreferOfSound(sound, currentUser));
      }
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return sounds;
  }

  @POST
  @Path("/streams/tags/{tag}")
  @Produces(MediaType.APPLICATION_JSON)
  @ResourceAllowed
  public List<Sound> listSoundsByTag(@NotNull @PathParam("tag") String tagLabel,
      @QueryParam("pageNum") Integer pageNum, @QueryParam("soundsPerPage") Integer soundsPerPage) {
    pageNum = (null == pageNum) ? 0 : pageNum;
    soundsPerPage = (null == soundsPerPage) ? 15 : soundsPerPage;

    List<Sound> sounds = null;
    User currentUser = null;
    try {
      List<Tag> tags = new ArrayList<Tag>();
      Tag tag = new Tag();
      tag.setLabel(tagLabel);
      tag = tagService.get(tag, false);
      
      if (null == tag)
      {
        return Collections.emptyList();
      }
      tags.add(tag);
      
      currentUser = userService.getCurrentUser(req);
      
      if (null != currentUser)
      {
    	  currentUser.addTags(tags);
	      userService.saveUser(currentUser);
      }
      
      sounds = soundService.loadByTags(null, tags, pageNum, soundsPerPage);
      
      for (Sound sound: sounds)
      {
    	  sound.setUserPrefer(soundService.getUserPreferOfSound(sound, currentUser));
      }
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return sounds;
  }

  @GET
  @Path("/streams/history")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Sound> listUserHistory( @QueryParam("pageNum") Integer pageNum, @QueryParam("soundsPerPage") Integer soundsPerPage) {
    pageNum = (null == pageNum) ? 0 : pageNum;
    soundsPerPage = (null == soundsPerPage) ? 15 : soundsPerPage;

    List<Sound> sounds = null;
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      sounds = soundService.loadUserHistory(currentUser, pageNum, soundsPerPage);
      for (Sound sound: sounds)
      {
    	  sound.setUserPrefer(soundService.getUserPreferOfSound(sound, currentUser));
      }
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return sounds;
  }
  
  @POST
  @Path("/streams/tags")
  @Produces(MediaType.APPLICATION_JSON)
  @ResourceAllowed
  public List<Sound> listSoundsByTags(@NotNull final List<String> tagLabels,
      @QueryParam("pageNum") Integer pageNum, @QueryParam("soundsPerPage") Integer soundsPerPage) {
    pageNum = (null == pageNum) ? 0 : pageNum;
    soundsPerPage = (null == soundsPerPage) ? 15 : soundsPerPage;

    List<Sound> sounds = null;
    User currentUser = null;
    try {
      List<Tag> tags = new ArrayList<Tag>();
      for (String label: tagLabels)
      {
        Tag tag = new Tag();
        tag.setLabel(label);
        tag = tagService.get(tag, false);
        
        if (null == tag)
        {
          continue;
        }
        
        tags.add(tag);
      }
      
      currentUser = userService.getCurrentUser(req);
      
      if (null != currentUser)
      {
	      currentUser.addTags(tags);
	      userService.saveUser(currentUser);
      }
      
      sounds = soundService.loadByTags(currentUser, tags, pageNum, soundsPerPage);
      
      if (sounds.size() < soundsPerPage)
      {
        sounds.addAll(soundSocialService.recommandRandomSounds(currentUser, soundsPerPage-sounds.size()));
      }
      
      for (Sound sound: sounds)
      {
    	  sound.setUserPrefer(soundService.getUserPreferOfSound(sound, currentUser));
      }
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return sounds;
  }
  
  @POST
  @Path("/streams/{userAlias}")
  @Produces(MediaType.APPLICATION_JSON)
  @ResourceAllowed
  public List<Sound> listUsersSounds(@QueryParam("pageNum") Integer pageNum,
      @PathParam("userAlias") String userAlias, @QueryParam("soundsPerPage") Integer soundsPerPage) {
    pageNum = (null == pageNum) ? 0 : pageNum;
    soundsPerPage = (null == soundsPerPage) ? 15 : soundsPerPage;

    List<Sound> sounds = null;
    User user = null;
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      user = userService.getUserByAlias(userAlias);
      if (null == user) {
        throw new WebApplicationException(Status.NOT_FOUND);
      }

      sounds = soundService.getSoundsByUser(user, currentUser, pageNum, soundsPerPage);
      
      for (Sound sound: sounds)
      {
    	  sound.setUserPrefer(soundService.getUserPreferOfSound(sound, currentUser));
      }
    } catch (WebApplicationException e) {
      throw e;
    } catch (SoundException e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return sounds;
  }

  @POST
  @Path("/streams")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({Constant.ADMIN_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE, Constant.USER_ROLE})
  public List<Sound> listObservingSounds(@QueryParam("pageNum") Integer pageNum,
      @QueryParam("soundsPerPage") Integer soundsPerPage) {
    pageNum = (null == pageNum) ? 0 : pageNum;
    soundsPerPage = (null == soundsPerPage) ? 15 : soundsPerPage;

    List<Sound> sounds = null;
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      sounds = soundService.getObservingSounds(currentUser, pageNum, soundsPerPage);
      
      for (Sound sound: sounds)
      {
    	  sound.setUserPrefer(soundService.getUserPreferOfSound(sound, currentUser));
      }
    } catch (SoundException e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    return sounds;
  }

  @GET
  @Path("/toupload")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({Constant.ADMIN_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE, Constant.USER_ROLE})
  public Sound getSoundToUpload() {
    Sound sound = null;
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      sound = soundService.getUnfinishedUpload(currentUser);
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    return sound;
  }

  @GET
  @Path("/hasNew")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({Constant.ADMIN_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE, Constant.USER_ROLE})
  public Long getNewSounds(@QueryParam("startTime") String time) {
    long soundCount = 0;
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
      soundCount = soundService.hasNewSounds(currentUser, startTime);
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    return soundCount;
  }
  
  @GET
  @Path("/hasNewCreated/{userAlias}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({Constant.ADMIN_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE, Constant.USER_ROLE})
  public Long getNewCreatedSounds(@QueryParam("startTime") String time,
                                  @NotNull @PathParam("userAlias") String userAlias) {
    long soundCount = 0;
    User user = null;
    try {
      user = userService.getUserByAlias(userAlias);
      Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
      soundCount = soundService.hasNewCreated(user, startTime);
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    return soundCount;
  }

  @GET
  @Path("/{soundAlias}/info")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed({Constant.ADMIN_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE, Constant.USER_ROLE})
  public String getSoundInfo(@NotNull @PathParam("soundAlias") String soundAlias) {
    String info = null;
    try {
      Sound sound = soundService.load(null, soundAlias);
      sound.setUserPrefer(soundService.getUserPreferOfSound(sound, userService.getCurrentUser(req)));
      info = remoteStorageService.getSoundInfo(sound.getProfile().getRemoteId());
    } catch (Exception e) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    return info;
  }
}

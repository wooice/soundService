package com.sound.service.endpoint;

import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.constant.Constant;
import com.sound.model.Sound.QueueNode;
import com.sound.model.User;
import com.sound.service.sound.itf.SoundService;

@Component
@Path("/storage")
@RolesAllowed({Constant.ADMIN_ROLE, Constant.USER_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE})
public class StorageServiceEndpoint {

  Logger logger = Logger.getLogger(StorageServiceEndpoint.class);

  @Autowired
  com.sound.service.storage.itf.RemoteStorageServiceV2 remoteStorageService;

  @Autowired
  com.sound.service.user.itf.UserService userService;

  @Autowired
  SoundService soundService;

  @Context
  HttpServletRequest req;

  @GET
  @Path("/upload/sound/{fileKey}")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, String> getSoundUploadInfo(@PathParam("fileKey") String fileKey) {
    Map<String, String> info = null;
    try {
      info = remoteStorageService.getSoundUploadInfo(fileKey);
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return info;
  }

  @GET
  @Path("/upload/image/{fileKey}")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, String> getImageUploadInfo(@PathParam("fileKey") String fileKey) {
    Map<String, String> info = null;
    try {
      info = remoteStorageService.getImageUploadInfo(fileKey);
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return info;
  }

  @POST
  @Path("/upload")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response upload(@NotNull JsonObject inputJsonObj) {
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      String fileName = inputJsonObj.getString("fileName");
      String originName = inputJsonObj.getString("originName");

      QueueNode node = new QueueNode();
      node.setFileName(fileName);
      node.setOriginFileName(originName);
      node.setOwner(currentUser);

      soundService.enqueue(node);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    return Response.status(Response.Status.OK).build();
  }

}

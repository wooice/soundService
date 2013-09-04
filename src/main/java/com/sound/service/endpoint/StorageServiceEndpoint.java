package com.sound.service.endpoint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.constant.Constant;
import com.sound.exception.RemoteStorageException;
import com.sound.model.OssAuth;
import com.sound.model.Sound;
import com.sound.model.Sound.QueueNode;
import com.sound.model.Sound.SoundProfile.SoundPoster;
import com.sound.model.User;
import com.sound.model.User.UserProfile;
import com.sound.model.enums.FileType;
import com.sound.model.file.SoundLocal;
import com.sound.service.sound.itf.SoundService;
import com.sound.util.JsonHandler;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Component
@Path("/storage")
@RolesAllowed({Constant.ADMIN_ROLE, Constant.USER_ROLE})
public class StorageServiceEndpoint {

  Logger logger = Logger.getLogger(StorageServiceEndpoint.class);

  @Autowired
  com.sound.service.storage.itf.RemoteStorageService remoteStorageService;

  @Autowired
  com.sound.service.user.itf.UserService userService;

  @Autowired
  SoundService soundService;

  @Context
  HttpServletRequest req;

  @GET
  @Path("/ossauth")
  public OssAuth getOSSAuth() {
    OssAuth dto = loadOssAuthDto();
    return dto;
  }

  @GET
  @Path("/downloadurl/{type}/{file}")
  public Response getDownloadUrl(@NotNull @PathParam("type") String type,
      @NotNull @PathParam("file") String file) {
    URL url = null;
    try {
      url = remoteStorageService.generateDownloadUrl(file, FileType.getFileType(type));
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity("Failed to get download url of type " + type + " and file " + file).build();
    }

    return Response.status(Status.OK).entity(url.toString()).build();
  }

  @GET
  @Path("/uploadurl/{type}/{file}")
  public Response getUploadUrl(@NotNull @PathParam("type") String type,
      @NotNull @PathParam("file") String file) {
    URL url = null;
    try {
      url = remoteStorageService.generateUploadUrl(file, FileType.getFileType(type));
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity("Failed to get upload url of type " + type + " and file " + file).build();
    }
    return Response.status(Status.OK).entity(url.toString()).build();
  }

  @POST
  @Path("/upload/avatar")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response uploadUserAvatar(@FormDataParam("file") InputStream uploadedInputStream,
      @FormDataParam("file") FormDataContentDisposition fileDetail,
      @FormDataParam("fileLenth") InputStream fileLenthStream) {

    try {
      User user = userService.getCurrentUser(req);

      if (null == user) {
        return Response.status(Response.Status.FORBIDDEN).entity(JsonHandler.toJson("failed"))
            .build();
      }

      StringWriter writer = new StringWriter();
      IOUtils.copy(fileLenthStream, writer);
      String fileLenth = writer.toString();

      SoundLocal soundLocal = new SoundLocal();

      String[] temp = fileDetail.getFileName().split("\\.");
      String avatarName = user.getProfile().getAlias() + "." + temp[1];
      soundLocal.setFileName(avatarName);
      soundLocal.setSoundStream(uploadedInputStream);
      soundLocal.setLength(Long.parseLong(fileLenth));
      remoteStorageService.upload(soundLocal, FileType.IMAGE);

      UserProfile profile = user.getProfile();
      profile.setAvatorUrl(avatarName);
      userService.updateUserBasicProfile(user, profile);

    } catch (Exception e) {
      logger.error(e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("unable to upload sound.").build();
    }

    return Response.status(Response.Status.OK).entity(JsonHandler.toJson("true")).build();
  }

  @POST
  @Path("/upload/poster")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response uploadSoundPoster(@FormDataParam("file") InputStream uploadedInputStream,
      @FormDataParam("file") FormDataContentDisposition fileDetail,
      @FormDataParam("fileName") InputStream nameStream,
      @FormDataParam("length") InputStream posterlengthStream) {

    try {
      StringWriter writer = new StringWriter();
      IOUtils.copy(nameStream, writer);
      String fileName = writer.toString();

      writer = new StringWriter();
      IOUtils.copy(posterlengthStream, writer);
      String length = writer.toString();

      SoundLocal soundLocal = new SoundLocal();
      soundLocal.setFileName(fileName);
      soundLocal.setSoundStream(uploadedInputStream);
      soundLocal.setLength(Long.parseLong(length));
      remoteStorageService.upload(soundLocal, FileType.IMAGE);

      Sound sound = soundService.loadByRemoteId(fileName.split("\\.")[0]);

      if (null != sound) {
        String[] fileNames = fileName.split("\\.");
        SoundPoster poster = new SoundPoster();
        poster.setPosterId(fileNames[0]);
        poster.setExtension(fileNames[1]);
        sound.getProfile().setPoster(poster);

        soundService.updateProfile(sound.getProfile());
      }
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("unable to upload sound.").build();
    }

    return Response.status(Response.Status.OK).entity(JsonHandler.toJson("true")).build();
  }

  @POST
  @Path("/upload")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response upload(@FormDataParam("file") InputStream uploadedInputStream,
      @FormDataParam("file") FormDataContentDisposition fileDetail,
      @FormDataParam("fileName") InputStream nameStream) {
    File queueNodeFile = null;
    User currentUser = null;
    try {
      currentUser = userService.getCurrentUser(req);
      
      StringWriter writer = new StringWriter();
      IOUtils.copy(nameStream, writer);
      String fileName = writer.toString();

      String queueNodeFilePath = Constant.UPLOAD_QUEUE_FOLDER + File.separator + fileName;
      queueNodeFile = new File(queueNodeFilePath);
      if (!queueNodeFile.getParentFile().exists()) {
        queueNodeFile.getParentFile().mkdirs();
      }
      IOUtils.copy(uploadedInputStream, new FileOutputStream(queueNodeFile));

      QueueNode node = new QueueNode();
      node.setFileName(fileName);
      node.setOriginFileName(fileDetail.getFileName());
      node.setOwnerAlias("robot");

      soundService.checkUploadCap(currentUser, queueNodeFile);
      soundService.enqueue(node);
    } catch (Exception e) {
      if (null != queueNodeFile) {
        queueNodeFile.delete();
      }
      logger.error(e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("unable to upload sound.").build();
    }

    return Response.status(Response.Status.OK).entity(JsonHandler.toJson("true")).build();
  }

  @POST
  @Path("/upload")
  public Response remove(@FormParam("fileName") String fileName) {
    try {
      remoteStorageService.delete(fileName, FileType.SOUND);
    } catch (RemoteStorageException e) {
      logger.error(e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("unable to upload sound.").build();
    }

    return Response.status(Response.Status.OK).entity(JsonHandler.toJson("true")).build();
  }

  private OssAuth loadOssAuthDto() {
    OssAuth dto = new OssAuth();
    PropertiesConfiguration config = remoteStorageService.getRemoteStorageConfig();
    dto.setAccessId(config.getString("ACCESS_ID"));
    dto.setAccessPassword(config.getString("ACCESS_KEY"));
    dto.setSoundBucket(config.getString("SoundBucket"));
    dto.setImageBucket(config.getString("ImageBucket"));
    return dto;
  }

}

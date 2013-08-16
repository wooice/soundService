package com.sound.service.endpoint;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.dto.UserBasicProfileDTO;
import com.sound.dto.UserSnsProfileDTO;
import com.sound.exception.UserException;
import com.sound.model.User;
import com.sound.util.JsonHandler;
import com.sun.jersey.multipart.FormDataParam;

@Component
@Path("/user")
public class UserServiceEndpoint {

  Logger logger = Logger.getLogger(UserServiceEndpoint.class);

  @Autowired
  com.sound.service.user.itf.UserService userService;

  @GET
  @Path("/{userAlias}/checkAlias")
  public Response checkAlias(@NotNull @PathParam("userAlias") String userAlias) {
    User user = null;

    try {
      user = userService.getUserByAlias(userAlias);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to get user by alias " + userAlias)).build();
    }
    String result = (null == user) ? "true" : "false";

    return Response.status(Status.OK).entity(result).build();
  }

  @GET
  @Path("/{emailAddress}/checkEmail")
  public Response checkEmail(@NotNull @PathParam("emailAddress") String emailAddress) {
    User user = null;
    try {
      user = userService.getUserByEmail(emailAddress);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to check emailaddress " + emailAddress)).build();
    }
    String result = (null == user) ? "true" : "false";

    return Response.status(Status.OK).entity(result).build();
  }

  @PUT
  @Path("/create")
  public Response create(@NotNull @FormParam("userAlias") String userAlias,
      @NotNull @FormParam("emailAddress") String emailAddress,
      @NotNull @FormParam("password") String password) {
    try {
      userService.createUser(userAlias, emailAddress, password);
    } catch (UserException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to create user " + userAlias)).build();
    }

    return Response.status(Status.OK).entity("true").build();
  }

  @GET
  @Path("/{userAlias}")
  public Response load(@NotNull @PathParam("userAlias") String userAlias) {
    User user = null;
    try {
      user = userService.getUserByAlias(userAlias);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to load user " + userAlias)).build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson(user)).build();
  }


  @POST
  @Path("/updateBasic")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response updateUserBasicProfile(@NotNull @FormParam("userAlias") String userAlias,
      @NotNull @FormDataParam("basicProfile") UserBasicProfileDTO basicProfileDTO) {
    User user = null;
    try {
      user = userService.updateUserBasicProfile(userAlias, basicProfileDTO);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Cannot update user basic profile for " + userAlias)).build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson(user)).build();
  }

  @POST
  @Path("/updateSns")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response updateUserSnsProfile(@NotNull @FormParam("userAlias") String userAlias,
      @NotNull @FormDataParam("snsProfile") UserSnsProfileDTO snsProfileDTO) {
    User user = null;
    try {
      user = userService.updateUserSnsProfile(userAlias, snsProfileDTO);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Cannot update user sns profile for " + userAlias)).build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson(user)).build();
  }

  @POST
  @Path("/updatePassword")
  public Response updateUserPassword(@NotNull @FormParam("emailAddress") String emailAddress,
      @NotNull @FormParam("password") String password, @NotNull @FormParam("ip") String ip) {
    User user = null;
    try {
      user = userService.updatePassword(emailAddress, password, ip);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Cannot update password for " + emailAddress)).build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson(user)).build();
  }

}

package com.sound.service.endpoint;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.imageio.ImageIO;
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
import com.sound.exception.AuthException;
import com.sound.exception.UserException;
import com.sound.filter.authentication.ResourceAllowed;
import com.sound.model.User;

@Component
@Path("/auth")
@RolesAllowed({Constant.USER_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE, Constant.ADMIN_ROLE,
    Constant.GUEST_ROLE})
@ResourceAllowed
public class AuthServiceEndpoint {

  Logger logger = Logger.getLogger(UserServiceEndpoint.class);

  @Autowired
  com.sound.service.user.itf.UserService userService;
  
  @Autowired
  com.sound.service.user.itf.MessageService messageService;

  @Context
  HttpServletRequest req;

  @GET
  @Path("/confirmEmail/{confirmCode}")
  public Response confirmEmailAddress(@NotNull @PathParam("confirmCode") String confirmCode) {
    try {
      userService.confirmEmailAddress(confirmCode);
    } catch (UserException e) {
      throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(e.getMessage()).build());
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }

    return Response.status(Status.OK).build();
  }

  @GET
  @Path("/resetRequest/{action}/{code}")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Boolean> verifyResetRequest(@NotNull @PathParam("action") String action,
      @NotNull @PathParam("code") String code) {
    boolean result;
    try {
      if (userService.verifyResetRequest(action, code)) {
        result = true;
      } else {
        throw new WebApplicationException(Status.FORBIDDEN);
      }
    } catch (WebApplicationException e) {
      throw e;
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    Map<String, Boolean> response = new HashMap<String, Boolean>();
    response.put("result", result);

    return response;
  }

  @POST
  @Path("/updatePassword")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUserPassword(@NotNull JsonObject inputJsonObj) {

    try {
      String code = inputJsonObj.getString("confirmCode");

      if (!userService.verifyResetRequest("confirm", code)) {
        return Response.status(Status.FORBIDDEN).entity("You don't have rights to update password")
            .build();
      }

      String oldPassword = inputJsonObj.getString("oldPassword");
      String newPassword = inputJsonObj.getString("newPassword");

      userService.updatePassword(code, oldPassword, newPassword, null);
    } catch (AuthException e) {
      logger.error(e);
      return Response.status(Status.FORBIDDEN).build();
    } catch (UserException e) {
      logger.error(e);
      return Response.status(Status.BAD_REQUEST).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }

    return Response.status(Status.OK).build();
  }

  @POST
  @Path("/resetPassword")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response resetUserPassword(@NotNull JsonObject inputJsonObj) {

    try {
      String code = inputJsonObj.getString("confirmCode");

      if (!userService.verifyResetRequest("confirm", code)) {
        return Response.status(Status.FORBIDDEN).entity("You don't have rights to update password")
            .build();
      }

      String password = inputJsonObj.getString("newPassword");

      userService.resetPassword(code, password, null);
    } catch (AuthException e) {
      logger.error(e);
      return Response.status(Status.FORBIDDEN).build();
    } catch (UserException e) {
      logger.error(e);
      return Response.status(Status.BAD_REQUEST).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }

    return Response.status(Status.OK).build();
  }

  @GET
  @Path("/isAlive")
  @Produces(MediaType.APPLICATION_JSON)
  public User isAlive() {
    User user = null;
    try {
      user = userService.getCurrentUser(req);
      user.setUnreadMsgs(messageService.countUnreadMessage(user));
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return user;
  }

  @GET
  @Path("/verify/image")
  @Produces("image/png")
  public Response getVerifyImage() {
    BufferedImage image = userService.generateVerifyImage(req);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      ImageIO.write(image, "png", baos);
    } catch (IOException e) {
      e.printStackTrace();
    }
    byte[] imageData = baos.toByteArray();

    return Response.ok(new ByteArrayInputStream(imageData)).build();
  }
}

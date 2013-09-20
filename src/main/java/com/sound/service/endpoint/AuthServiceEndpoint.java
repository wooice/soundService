package com.sound.service.endpoint;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.constant.Constant;
import com.sound.model.User;
import com.sound.util.JsonHandler;

@Component
@Path("/auth")
@RolesAllowed({Constant.USER_ROLE, Constant.GUEST_ROLE})
public class AuthServiceEndpoint {


  Logger logger = Logger.getLogger(UserServiceEndpoint.class);

  @Autowired
  com.sound.service.user.itf.UserService userService;

  @GET
  @Path("/confirmEmail/{confirmCode}")
  public Response confirmEmailAddress(@NotNull @PathParam("confirmCode") String confirmCode) {
    try {
      userService.confirmEmailAddress(confirmCode);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(("Cannot confirm email")).build();
    }

    return Response.status(Status.OK).entity("Confirm Successfully").build();
  }

  @GET
  @Path("/resetRequest/{action}/{code}")
  public Response verifyResetRequest(@NotNull @PathParam("action") String action,
                                      @NotNull @PathParam("code") String code) {
    boolean result;
    try {
      if(userService.verifyResetRequest(action, code))
      {
        result = true;
      }
      else
      {
        result = false;
      }
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(("Cannot confirm email")).build();
    }

    Map<String, Boolean> response = new HashMap<String, Boolean>();
    response.put("result", result);

    return Response.status(Status.OK).entity(JsonHandler.toJson(response)).build();
  }

  @POST
  @Path("/updatePassword")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUserPassword(@NotNull JSONObject inputJsonObj) {
    User user = null;

    try {
      String code = inputJsonObj.getString("confirmCode");

      if (!userService.verifyResetRequest("confirm", code))
      {
        return Response.status(Status.FORBIDDEN)
            .entity("You don't have rights to update password").build();
      }
      
      String oldPassword = inputJsonObj.getString("oldPassword");
      String newPassword = inputJsonObj.getString("newPassword");
   
      user = userService.updatePassword(code, oldPassword, newPassword, null);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Cannot update password for " + user.getProfile().getAlias())).build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson(user)).build();
  }
}

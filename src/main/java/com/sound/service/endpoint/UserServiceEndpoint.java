package com.sound.service.endpoint;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.exception.UserException;
import com.sound.model.User;
import com.sound.model.User.UserEmail.EmailSetting;
import com.sound.model.User.UserExternal;
import com.sound.model.User.UserProfile;
import com.sound.util.JsonHandler;

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
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUserBasicProfile(@NotNull @FormParam("userAlias") String userAlias,
      @NotNull JSONObject inputJsonObj) {
    User user = null;
    UserProfile profile = new UserProfile();
    try {
      profile.setAlias(inputJsonObj.getString("alias"));
      profile.setAvatorUrl(inputJsonObj.getString("avatorUrl"));
      profile.setFirstName(inputJsonObj.getString("firstName"));
      profile.setLastName(inputJsonObj.getString("lastName"));
      profile.setCity(inputJsonObj.getString("city"));
      profile.setCountry(inputJsonObj.getString("country"));
      JSONArray occs = inputJsonObj.getJSONArray("occupations");
      List<Integer> occList = new ArrayList<Integer>();
      for (int i = 0; i < occs.length(); i++) {
        occList.add(occs.getInt(i));
      }
      profile.setOccupations(occList);
      user = userService.updateUserBasicProfile(userAlias, profile);
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
      @NotNull UserExternal external) {
    User user = null;
    try {
      user = userService.updateUserSnsProfile(userAlias, external);
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

  @PUT
  @Path("/addEmail/{userAlias}/{emailAddress}")
  public Response addEmailAddress(@NotNull @PathParam("userAlias") String userAlias,
      @NotNull @PathParam("emailAddress") String emailAddress) {
    User user = null;
    try {
      user = userService.addEmailAddress(userAlias, emailAddress);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Cannot add email " + emailAddress + " for user " + userAlias)).build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson(user)).build();
  }

  @PUT
  @Path("/sendEmailConfirm/{userAlias}/{emailAddress}")
  public Response sendEmailAddressConfirmation(@NotNull @PathParam("userAlias") String userAlias,
      @NotNull @PathParam("emailAddress") String emailAddress) {
    try {
      userService.sendEmailAddressConfirmation(userAlias, emailAddress);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Cannot send confirmation to email " + emailAddress + " for user " + userAlias))
          .build();
    }

    return Response.status(Status.OK).entity("send confirmation email successfully").build();
  }

  @GET
  @Path("/changeContactEmail/{userAlias}/{emailAddress}")
  public Response changeContactEmail(@NotNull @PathParam("userAlias") String userAlias,
      @NotNull @PathParam("emailAddress") String emailAddress) {
    try {
      userService.changeContactEmailAddress(userAlias, emailAddress);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Cannot change contact email " + emailAddress + " for user " + userAlias))
          .build();
    }

    return Response.status(Status.OK).entity("change contact email successfully").build();
  }

  @PUT
  @Path("/updateEmailSetting/{emailAddress}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response saveProfile(@NotNull @PathParam("emailAddress") String emailAddress,
      @NotNull EmailSetting setting) {
    User user = null;
    try {
      user = userService.updateEmailSetting(emailAddress, setting);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Cannot update email setting of " + emailAddress)).build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson(user)).build();
  }

  @PUT
  @Path("/sendMessage")
  public Response sendUserMessage(@NotNull @FormParam("from") String fromUser,
      @NotNull @FormParam("to") String toUser, @NotNull @FormParam("topic") String topic,
      @NotNull @FormParam("content") String content) {
    try {
      userService.sendUserMessage(fromUser, toUser, topic, content);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Cannot send user message from " + fromUser + " to " + toUser)).build();
    }

    return Response.status(Status.OK).entity("send sucessfully").build();

  }

  @DELETE
  @Path("/removeMessage")
  public Response removeUserMessage(@NotNull @FormParam("from") String fromUser,
      @NotNull @FormParam("to") String toUser, @NotNull @FormParam("messageId") String messageId) {
    try {
      userService.removeUserMessage(fromUser, toUser, messageId);
    } catch (Exception e) {
      logger.error(e);
      return Response
          .status(Status.INTERNAL_SERVER_ERROR)
          .entity(
              ("Cannot remove user message " + messageId + " from " + fromUser + " to " + toUser))
          .build();
    }

    return Response.status(Status.OK).entity("remove sucessfully").build();
  }

}

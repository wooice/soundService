package com.sound.service.endpoint;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
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
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.constant.Constant;
import com.sound.exception.UserException;
import com.sound.model.User;
import com.sound.model.User.UserEmail.EmailSetting;
import com.sound.model.User.UserExternal;
import com.sound.model.User.UserProfile;
import com.sound.model.UserMessage;
import com.sound.util.JsonHandler;

@Component
@Path("/user")
@RolesAllowed({Constant.ADMIN_ROLE, Constant.USER_ROLE})
public class UserServiceEndpoint {

  Logger logger = Logger.getLogger(UserServiceEndpoint.class);

  @Autowired
  com.sound.service.user.itf.UserService userService;

  @Context
  HttpServletRequest req;

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
  public Response updateUserBasicProfile(@NotNull JSONObject inputJsonObj) {
    User user = null;
    UserProfile profile = new UserProfile();
    try {
      user = userService.getCurrentUser(req);

      // profile.setAlias(inputJsonObj.getString("alias"));
      profile.setFirstName(inputJsonObj.getString("firstName"));
      profile.setLastName(inputJsonObj.getString("lastName"));
      profile.setCity(inputJsonObj.getString("city"));
      profile.setCountry(inputJsonObj.getString("country"));
      profile.setDescription(inputJsonObj.getString("description"));
      // JSONArray occs = inputJsonObj.getJSONArray("occupations");
      // List<Integer> occList = new ArrayList<Integer>();
      // for (int i = 0; i < occs.length(); i++) {
      // occList.add(occs.getInt(i));
      // }
      // profile.setOccupations(occList);
      user = userService.updateUserBasicProfile(user, profile);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Cannot update user basic profile for " + user.getProfile().getAlias())).build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson(user)).build();
  }

  @POST
  @Path("/updateSns")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUserSnsProfile(@NotNull UserExternal external) {
    User user = null;
    try {
      user = userService.getCurrentUser(req);
      user = userService.updateUserSnsProfile(user, external);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Cannot update user sns profile for " + user.getProfile().getAlias())).build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson(user)).build();
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
  @Path("/messages/send")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response sendUserMessage(@NotNull JSONObject inputJsonObj) {
    User curUser = null;
    try {
      String toUser = inputJsonObj.getString("toUser");
      String topic = inputJsonObj.getString("topic");
      String content = inputJsonObj.getString("content");
      curUser = userService.getCurrentUser(req);
      User to = userService.getUserByAlias(toUser);
      userService.sendUserMessage(curUser, to, topic, content);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Cannot send user message from ")).build();
    }

    return Response.status(Status.OK).entity("send sucessfully").build();
  }

  @POST
  @Path("/messages/mark")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response markUserMessage(@NotNull JSONObject inputJsonObj) {
    try {
      String messageId = inputJsonObj.getString("id");
      String status = inputJsonObj.getString("status");
      userService.markUserMessage(messageId, status);
    } catch (Exception e) {
      logger.error(e);
      return Response
          .status(Status.INTERNAL_SERVER_ERROR)
          .entity(
              ("Cannot mark user message "))
          .build();
    }

    return Response.status(Status.OK).entity("remove sucessfully").build();
  }

  @GET
  @Path("/messages")
  public Response getMessages(@QueryParam("pageNum") Integer pageNum,
      @QueryParam("perPage") Integer perPage, @QueryParam("status") String status) {
    User curUser = null;
    List<UserMessage> messages = null;
    try {
      curUser = userService.getCurrentUser(req);
      messages = userService.getUserMessages(curUser, status, pageNum, perPage);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity((e.getMessage())).build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson(messages)).build();
  }

  @GET
  @Path("/isAlive")
  public Response isAlive() {
    User user = null;
    try {
      user = userService.getCurrentUser(req);

      if (null == user) {
        throw new RuntimeException("You are not logged in.");
      }
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.FORBIDDEN).entity(e.getMessage()).build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson(user)).build();
  }

  @POST
  @Path("/logout")
  public Response logout() {
    User user = null;
    try {
      user = userService.getCurrentUser(req);

      if (null == user) {
        throw new RuntimeException("You are not logged in.");
      }
      HttpSession session = req.getSession(false);

      if (null != session) {
        session.invalidate();
      }
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.FORBIDDEN).entity(e.getMessage()).build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson("true")).build();
  }

  @POST
  @Path("/submitPassChange")
  public Response forgetPass() {
    User user = null;
    try {
      user = userService.getCurrentUser(req);
      if (user == null) {
        return Response.status(Status.FORBIDDEN).entity("User not logged in.").build();
      }
      userService.sendChangePassLink(user.getProfile().getAlias());
    } catch (UserException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(("Failed to create user "))
          .build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson("true")).build();
  }
}

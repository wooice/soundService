package com.sound.service.endpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.sound.model.User.UserRole;
import com.sound.util.JsonHandler;

@Component
@Path("/guest")
@RolesAllowed({Constant.ADMIN_ROLE, Constant.GUEST_ROLE})
public class GuestServiceEndpoint {

  Logger logger = Logger.getLogger(UserServiceEndpoint.class);

  @Autowired
  com.sound.service.user.itf.UserService userService;

  @Context
  HttpServletRequest req;

  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response login(@NotNull JSONObject inputJsonObj) {
    User user = null;

    try {
      String userId = inputJsonObj.getString("userId");
      String password = inputJsonObj.getString("password");

      String emailRegex =
          "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
      Pattern regex = Pattern.compile(emailRegex);
      Matcher matcher = regex.matcher(userId);
      boolean isEmail = matcher.matches();

      if (isEmail) {
        user = userService.getUserByEmail(userId);
      } else {
        user = userService.getUserByAlias(userId);
      }

      if (null == user) {
        throw new RuntimeException("The user " + userId + " does not exist.");
      }

      if (!user.getAuth().getPassword().equals(password)) {
        throw new RuntimeException("Password is not correct.");
      }

      HttpSession session = req.getSession(true);
      session.setAttribute("userAlias", user.getProfile().getAlias());

      List<String> roles = new ArrayList<String>();
      for (UserRole role : user.getUserRoles()) {
        roles.add(role.getRole());
      }
      session.setAttribute("userRoles", roles);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson(user)).build();
  }

  @GET
  @Path("/{userAlias}/checkAlias")
  public Response checkAlias(@NotNull @PathParam("userAlias") String userAlias) {
    User user = null;
    userService.getCurrentUser(req);
    try {
      user = userService.getUserByAlias(userAlias);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to get user by alias " + userAlias)).build();
    }
    Map<String, String> result = new HashMap<String, String>();
    result.put("unique", (null == user) ? "true" : "false");

    return Response.status(Status.OK).entity(JsonHandler.toJson(result)).build();
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
    Map<String, String> result = new HashMap<String, String>();
    result.put("unique", (null == user) ? "true" : "false");

    return Response.status(Status.OK).entity(JsonHandler.toJson(result)).build();
  }

  @PUT
  @Path("/create")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response create(@NotNull JSONObject inputJsonObj) {
    User user = null;
    try {
      String userAlias = inputJsonObj.getString("userAlias");
      String emailAddress = inputJsonObj.getString("emailAddress");
      String password = inputJsonObj.getString("password");
      user = userService.createUser(userAlias, emailAddress, password);
    } catch (UserException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(("Failed to create user "))
          .build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson(user)).build();
  }

  @PUT
  @Path("/reportForget")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response forgetPass(@NotNull JSONObject inputJsonObj) {
    try {
      String emailAddress = inputJsonObj.getString("emailAddress");
      userService.sendChangePassLink(emailAddress);
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

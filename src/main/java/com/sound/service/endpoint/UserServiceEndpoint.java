package com.sound.service.endpoint;

import java.util.HashMap;
import java.util.Map;

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
import com.sound.exception.UserException;
import com.sound.model.User;
import com.sound.model.User.UserEmail;
import com.sound.model.User.UserEmail.EmailSetting;
import com.sound.model.User.UserExternal;
import com.sound.model.User.UserProfile;

@Component
@Path("/user")
@RolesAllowed({Constant.ADMIN_ROLE, Constant.USER_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE})
public class UserServiceEndpoint {

  Logger logger = Logger.getLogger(UserServiceEndpoint.class);

  @Autowired
  com.sound.service.user.itf.UserService userService;

  @Context
  HttpServletRequest req;

  @GET
  @Path("/{userAlias}")
  @Produces(MediaType.APPLICATION_JSON)
  public User load(@NotNull @PathParam("userAlias") String userAlias) {
    User user = null;
    User curUser = null;
    try {
      user = userService.getUserByAlias(userAlias);

      if (null == user) {
        throw new WebApplicationException(Status.NOT_FOUND);
      }
      curUser = userService.getCurrentUser(req);
      user.setUserPrefer(userService.getUserPrefer(curUser, user));
    } catch (WebApplicationException e) {
      throw e;
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return user;
  }

  @GET
  @Path("/{userAlias}/external")
  @Produces(MediaType.APPLICATION_JSON)
  public UserExternal loadExternal(@NotNull @PathParam("userAlias") String userAlias) {
    User user = null;
    try {
      user = userService.getUserByAlias(userAlias);

      if (null == user) {
        throw new WebApplicationException(Status.NOT_FOUND);
      }
    } catch (WebApplicationException e) {
      throw e;
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return user.getExternal();
  }

  @POST
  @Path("/updateBasic")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public User updateUserBasicProfile(@NotNull UserProfile userProfile) {
    User user = null;
    try {
      user = userService.getCurrentUser(req);

      user = userService.updateUserBasicProfile(user, userProfile);
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return user;
  }

  @POST
  @Path("/updateSns")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public User updateUserSnsProfile(@NotNull UserExternal external) {
    User user = null;
    try {
      user = userService.getCurrentUser(req);
      user = userService.updateUserSnsProfile(user, external);
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return user;
  }

  @PUT
  @Path("/addEmail/{emailAddress}")
  @Produces(MediaType.APPLICATION_JSON)
  public User addEmailAddress(
      @NotNull @PathParam("emailAddress") String emailAddress) {
    User user = null;
    try {
      user = userService.getCurrentUser(this.req);
      user = userService.addEmailAddress(user.getProfile().getAlias(), emailAddress);
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return user;
  }

  @PUT
  @Path("/sendEmailConfirm/{emailAddress}")
  public Response sendEmailAddressConfirmation(
      @NotNull @PathParam("emailAddress") String emailAddress) {
    User curUser = null;
    try {
      curUser =  userService.getCurrentUser(req);
      userService.sendEmailAddressConfirmation(curUser, emailAddress);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }

    return Response.status(Status.OK).build();
  }

  @POST
  @Path("/changeContactEmail/{emailAddress}")
  public Response changeContactEmail(
      @NotNull @PathParam("emailAddress") String emailAddress) {
    User curUser = null;
    String emailToVerify = null;
    try {
      curUser =  userService.getCurrentUser(req);
      emailToVerify = userService.changeContactEmailAddress(curUser, emailAddress);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }

    Map<String, String> result = new HashMap<String, String>();
    result.put("emailToVerify", emailToVerify);
    
    return Response.status(Status.OK).entity(result).build();
  }

  @PUT
  @Path("/updateEmailSetting/{emailAddress}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public User saveProfile(@NotNull @PathParam("emailAddress") String emailAddress,
      @NotNull EmailSetting setting) {
    User user = null;
    try {
      user = userService.updateEmailSetting(emailAddress, setting);
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return user;
  }

  @POST
  @Path("/logout")
  public Response logout() {
    User user = null;
    try {
      user = userService.getCurrentUser(req);

      if (null == user) {
        return Response.status(Status.UNAUTHORIZED).build();
      }
      HttpSession session = req.getSession(false);

      if (null != session) {
        session.invalidate();
      }
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.UNAUTHORIZED).build();
    }

    return Response.status(Status.OK).build();
  }

  @POST
  @Path("/submitPassChange")
  public Response forgetPass() {
    User user = null;
    try 
    {
      user = userService.getCurrentUser(req);
      UserEmail contactEmail = null;
      
      for (UserEmail email: user.getEmails())
      {
        if (email.isContact())
        {
          contactEmail = email;
        }
      }
      
      if (null == contactEmail) 
      {
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("NO_EMAIL_BIND").build();
      }
      
      if (!contactEmail.isConfirmed())
      {
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity("NOT_CONFIRMED").build();
      }
      
      userService.sendChangePassLink(user.getEmails().get(0).getEmailAddress());
    } catch (UserException e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }

    return Response.status(Status.OK).build();
  }

}

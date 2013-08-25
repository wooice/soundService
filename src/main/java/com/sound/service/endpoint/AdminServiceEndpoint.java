package com.sound.service.endpoint;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.sound.constant.Constant;
import com.sound.model.User;
import com.sound.util.JsonHandler;

@Path("/admin")
@RolesAllowed(Constant.ADMIN_ROLE)
public class AdminServiceEndpoint {

  Logger logger = Logger.getLogger(UserServiceEndpoint.class);

  @Autowired
  com.sound.service.user.itf.UserService userService;

  @Context
  HttpServletRequest req;

  @POST
  @Path("/grantRole")
  public Response grantRole(@NotNull @FormParam("userAlias") String userAlias,
      @NotNull @FormParam("role") String role) {
    User user = null;
    try {
      user = userService.getUserByAlias(userAlias);
      user = userService.grantRole(user, role);

      HttpSession session = req.getSession(false);

      if (null != session) {
        List<String> rolesInSession = new ArrayList<String>();
        rolesInSession.add(role);
        session.setAttribute("userRoles", rolesInSession);
      }
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to grant role to user " + userAlias)).build();
    }

    return Response.status(Status.OK).entity(JsonHandler.toJson(user)).build();
  }
}

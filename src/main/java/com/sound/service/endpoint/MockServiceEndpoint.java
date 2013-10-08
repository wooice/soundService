package com.sound.service.endpoint;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.model.User;
import com.sound.service.sound.impl.SoundDataService;
import com.sound.service.sound.itf.SoundService;

@Component
@Path("/mock")
public class MockServiceEndpoint {

  @Autowired
  com.sound.service.user.itf.UserService userService;

  @Autowired
  SoundService soundService;

  @Autowired
  SoundDataService soundDataService;

  @GET
  @Path("/create")
  public Response mock() {
    try {
      userService.deleteByAlias("robot");
      User user = userService.createUser("robot", "robot@wooice.com", "robot123");
      userService.grantRole(user, "admin");
    } catch (Exception e) {
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
    return Response.status(Status.OK).build();
  }
}

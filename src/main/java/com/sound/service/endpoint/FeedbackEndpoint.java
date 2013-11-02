package com.sound.service.endpoint;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.constant.Constant;
import com.sound.model.Feedback;
import com.sound.model.User;
import com.sound.service.sound.itf.FeedbackService;

@Component
@Path("/feedback")
@RolesAllowed({Constant.ADMIN_ROLE, Constant.USER_ROLE, Constant.SPRO_ROLE, Constant.PRO_ROLE})
public class FeedbackEndpoint {
  
  Logger logger = Logger.getLogger(FeedbackEndpoint.class);

  @Autowired
  FeedbackService feedbackService;
  
  @Autowired
  com.sound.service.user.itf.UserService userService;

  @Context
  HttpServletRequest req;
  
  @PUT
  @Path("/create")
  @Produces(MediaType.APPLICATION_JSON)
  public void create(@NotNull Feedback feedback) {
    try {
      User currentUser = userService.getCurrentUser(req);
      feedback.setUser(currentUser);
      feedbackService.create(feedback);
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
  }
  
  @GET
  @Path("/list")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Feedback> list(@NotNull @QueryParam("pageNum") Integer pageNum,
    @NotNull @QueryParam("perPage") Integer perPage) {
    List<Feedback> feedbacks = null;
    try {
      feedbacks = feedbackService.getFeedbacks(pageNum, perPage);
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return feedbacks;
  }
}

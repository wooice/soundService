package com.sound.service.endpoint;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.constant.Constant;
import com.sound.model.User;
import com.sound.model.UserMessage;

@Component
@Path("/message")
@RolesAllowed({Constant.ADMIN_ROLE, Constant.USER_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE})
public class MessageServiceEndpoint {

  Logger logger = Logger.getLogger(MessageServiceEndpoint.class);

  @Autowired
  com.sound.service.user.itf.UserService userService;

  @Autowired
  com.sound.service.user.itf.MessageService messageService;

  @Context
  HttpServletRequest req;

  @POST
  @Path("/send")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response sendUserMessage(@NotNull JsonObject inputJsonObj) {
    User curUser = null;
    try {
      String toUser = inputJsonObj.getString("toUser");
      String topic = inputJsonObj.getString("topic");
      String content = inputJsonObj.getString("content");
      curUser = userService.getCurrentUser(req);
      User to = userService.getUserByAlias(toUser);
      messageService.sendUserMessage(curUser, to, topic, content);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }

    return Response.status(Status.OK).build();
  }

  @PUT
  @Path("/reply")
  @Consumes(MediaType.APPLICATION_JSON)
  public UserMessage replyMessage(@NotNull JsonObject inputJsonObj) {
    User curUser = null;
    UserMessage reply = null;
    try {
      String msgId = inputJsonObj.getString("to_id");
      String toUser = inputJsonObj.getString("toUser");
      String content = inputJsonObj.getString("content");
      curUser = userService.getCurrentUser(req);
      UserMessage message = messageService.getUserMessage(curUser, msgId);
      User to = userService.getUserByAlias(toUser);

      reply = messageService.replyMessage(message, curUser, to, "", content);
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return reply;
  }

  @POST
  @Path("/mark")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response markUserMessage(@NotNull JsonObject inputJsonObj) {
    try {
      String messageId = inputJsonObj.getString("id");
      String status = inputJsonObj.getString("status");
      User curUser = userService.getCurrentUser(req);
      messageService.markUserMessage(curUser, messageId, status);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }

    return Response.status(Status.OK).build();
  }

  @GET
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public UserMessage getMessage(@NotNull @PathParam("id") String id) {
    UserMessage message = null;
    try {
      User curUser = userService.getCurrentUser(req);
      message = messageService.getUserMessage(curUser, id);
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return message;
  }

  @GET
  @Path("/list")
  @Produces(MediaType.APPLICATION_JSON)
  public List<UserMessage> getMessages(@QueryParam("pageNum") Integer pageNum,
      @QueryParam("perPage") Integer perPage) {
    User curUser = null;
    List<UserMessage> messages = null;
    try {
      curUser = userService.getCurrentUser(req);
      messages = messageService.getUserMessages(curUser, pageNum, perPage);
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return messages;
  }

  @HEAD
  @Path("/count")
  @Produces(MediaType.APPLICATION_JSON)
  public Response countMessages() {
    User curUser = null;
    long count = 0;
    try {
      curUser = userService.getCurrentUser(req);
      count = messageService.countUserMessage(curUser);
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    return Response.status(Status.OK).header("result-length", count).build();
  }

  @GET
  @Path("/{msgId}/replies")
  @Produces(MediaType.APPLICATION_JSON)
  public List<UserMessage> getReplies(@PathParam("msgId") String msgId,
      @QueryParam("pageNum") Integer pageNum, @QueryParam("perPage") Integer perPage) {
    User curUser = null;
    UserMessage message = null;
    try {
      curUser = userService.getCurrentUser(req);
      message = messageService.getUserMessage(curUser, msgId);
    } catch (Exception e) {
      logger.error(e);
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    if (null == message || null == message.getReplies()) {
      return null;
    } else {
      return message.getReplies();
    }
  }
}

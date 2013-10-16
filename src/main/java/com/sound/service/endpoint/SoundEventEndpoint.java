package com.sound.service.endpoint;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.SseFeature;

import com.sound.constant.Constant;

@Path("events")
@RolesAllowed({Constant.ADMIN_ROLE,Constant.USER_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE, Constant.USER_ROLE})
public class SoundEventEndpoint {

  EventOutput eventOutput = null;

  @Context
  HttpServletRequest req;

  @GET
  @Produces(SseFeature.SERVER_SENT_EVENTS)
  public EventOutput getNotice() {
    HttpSession session = req.getSession(false);
    if (null != session) {
      if (null == session.getAttribute("eventOutput")) {
        eventOutput = new EventOutput();
        session.setAttribute("eventOutput", eventOutput);
      } else {
        eventOutput = (EventOutput) session.getAttribute("eventOutput");
      }
    } else {
      throw new WebApplicationException(Status.FORBIDDEN);
    }
    return eventOutput;
  }

}

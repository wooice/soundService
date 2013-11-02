package com.sound.service.endpoint;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;

import com.sound.constant.Constant;
import com.sound.model.SoundInfringe;
import com.sound.model.User;
import com.sound.service.sound.itf.SoundInfingeService;

@Path("infringe")
@RolesAllowed({Constant.ADMIN_ROLE,Constant.USER_ROLE, Constant.PRO_ROLE, Constant.SPRO_ROLE, Constant.USER_ROLE})
public class SoundInfingeEndpoint {

  @Context
  SoundInfingeService soundInfingeService;
  
  @Autowired
  com.sound.service.user.itf.UserService userService;

  @Context
  HttpServletRequest req;
  
  @POST
  @Path("/{create}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response create(SoundInfringe soundInfringe)
  {
    User currentUser = userService.getCurrentUser(req);
    soundInfringe.getInformer().setUser(currentUser);
    soundInfingeService.create(soundInfringe);
    
    return Response.status(Status.OK).build();
  }
  
  @GET
  @Path("/{list}")
  public List<SoundInfringe> list(
       @NotNull @QueryParam("status") String status,
       @NotNull @QueryParam("pageNum") int pageNum,
       @NotNull @QueryParam("perPage") int perPage
  )
  {
    List<SoundInfringe> infinges = soundInfingeService.list(status, pageNum, perPage);
    return infinges;
  }
}

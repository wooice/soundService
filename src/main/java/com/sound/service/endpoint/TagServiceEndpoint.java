package com.sound.service.endpoint;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.constant.Constant;
import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.Tag;
import com.sound.model.Tag.TagCategory;
import com.sound.model.User;
import com.sound.service.sound.itf.SoundService;
import com.sound.util.JsonHandler;

@Component
@Path("/tag")
@RolesAllowed({Constant.ADMIN_ROLE, Constant.USER_ROLE})
public class TagServiceEndpoint {

  Logger logger = Logger.getLogger(TagServiceEndpoint.class);

  @Autowired
  com.sound.service.sound.itf.TagService tagService;

  @Autowired
  SoundService soundService;

  @Autowired
  com.sound.service.user.itf.UserService userService;

  @Context
  HttpServletRequest req;

  @PUT
  @Path("/{userAlias}/create/{tag}")
  public Response createTag(@NotNull @PathParam("tag") String label,
      @NotNull @PathParam("userAlias") String userAlias, @PathParam("userAlias") String categoryName) {
    User curUser = null;
    try {
      curUser = userService.getCurrentUser(req);
      tagService.getOrCreate(label, curUser, categoryName);
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(("Failed to create tag " + label)).build();
    }

    return Response.status(Response.Status.CREATED).entity("true").build();
  }

  @PUT
  @Path("/attach/{soundAlias}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response attachTagsToSound(@NotNull @PathParam("soundAlias") String soundAlias,
      @NotNull JSONObject inputJsonObj) {

    User curUser = null;
    try {
      curUser = userService.getCurrentUser(req);
      JSONArray tags = inputJsonObj.getJSONArray("tags");
      List<String> tagList = new ArrayList<String>();
      for (int i = 0; i < tags.length(); ++i) {
        tagList.add(tags.getString(i));
      }
      tagService.attachToSound(soundAlias, tagList, curUser);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("Cannot attach Tag because internal server error").build();
    }

    return Response.status(Response.Status.OK).entity("true").build();
  }

  @PUT
  @Path("/{userAlias}/detach/{soundAlias}")
  public Response detachTagsFromSound(@NotNull @PathParam("soundAlias") String soundAlias,
      @NotNull @FormParam("tags") List<String> tagLabels,
      @NotNull @PathParam("userAlias") String userAlias) {
    User curUser = null;
    try {
      curUser = userService.getCurrentUser(req);
      tagService.detachFromSound(soundAlias, tagLabels, curUser);
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("Cannot detach Tag because internal server error").build();
    }

    return Response.status(Response.Status.OK).entity("true").build();
  }

  @GET
  @Path("/list")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTagsContains(@NotNull @QueryParam("term") String term) {
    List<Tag> tags = null;
    try {
      tags = tagService.listTagsContains(term);
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("Cannot detach Tag because internal server error").build();
    }

    return Response.status(Response.Status.OK).entity(JsonHandler.toJson(tags)).build();
  }

  @GET
  @Path("/sounds/{label}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getSoundsByTag(@NotNull @PathParam("label") String tagLabel) {
    List<Sound> sounds = null;
    try {
      sounds = tagService.getSoundsWithTag(tagLabel);
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("Cannot get sounds by tag because internal server error").build();
    }

    return Response.status(Response.Status.OK).entity(sounds.toString()).build();
  }

  @GET
  @Path("/list/curated")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCuratedTags() {
    List<Tag> tags = null;
    try {
      tags = tagService.findCurated();
    } catch (SoundException e) {
      logger.error(e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("Cannot detach Tag because internal server error").build();
    }

    return Response.status(Response.Status.OK).entity(JsonHandler.toJson(tags)).build();
  }

  @GET
  @Path("/list/categories")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCategories() {
    List<TagCategory> categories = null;
    try {
      categories = tagService.listCategories();
    } catch (Exception e) {
      logger.error(e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("Cannot detach Tag because internal server error").build();
    }

    return Response.status(Response.Status.OK).entity(JsonHandler.toJson(categories)).build();
  }
}

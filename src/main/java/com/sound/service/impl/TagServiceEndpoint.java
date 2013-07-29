package com.sound.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.Tag;
import com.sound.service.sound.itf.SoundService;

@Component
@Path("/tag")
public class TagServiceEndpoint {

	Logger logger = Logger.getLogger(TagServiceEndpoint.class);
	
	@Autowired
	com.sound.service.sound.itf.TagService tagService;

	@Autowired
	SoundService soundService;

	@PUT
	@Path("/{userAlias}/create/{tag}")
	public Response createTag(
			@NotNull @PathParam("tag") String label, 
			@NotNull @PathParam("userAlias") String userAlias) 
	{
		try 
		{
			tagService.getOrCreate(label, userAlias);

		} 
		catch (SoundException e) 
		{
			logger.error(e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(e.getMessage())
					.build();
		}
		catch (Exception e)
		{
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Failed to create tag " + label)
					.build();
		}
		
		return Response.status(Response.Status.CREATED)
				.entity("true").build();
	}

	@PUT
	@Path("/{userAlias}/attach/{soundAlias}")
	public Response attachTagsToSound(
			@NotNull @PathParam("soundAlias") String soundAlias,
			@NotNull @FormParam("tags") List<String> tagLabels, 
			@NotNull @PathParam("userAlias") String userAlias) 
	{
		try 
		{
			tagService.attachToSound(soundAlias, tagLabels, userAlias);
		} catch (Exception e) {
			logger.error(e);
			return Response
					.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Cannot attach Tag because internal server error")
					.build();
		}
		
		return Response.status(Response.Status.OK).entity("true").build();
	}

	@PUT
	@Path("/{userAlias}/detach/{soundAlias}")
	public Response detachTagsFromSound(
			@NotNull @PathParam("soundAlias") String soundAlias,
			@NotNull @FormParam("tags") List<String> tagLabels, 
			@NotNull @PathParam("userAlias") String userAlias) {
		try 
		{
			tagService.detachFromSound(soundAlias, tagLabels, userAlias);
		} 
		catch (Exception e) {
			logger.error(e);
			return Response
					.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Cannot detach Tag because internal server error")
					.build();
		}
		
		return Response.status(Response.Status.OK).entity("true").build();
	}

	@GET
	@Path("/match/{pattern}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTagsContains(
			@NotNull @PathParam("pattern") String pattern) 
	{
		List<String> tagLabels = null;
		try {
			List<Tag> tags = tagService.listTagsContains(pattern);
			tagLabels = new ArrayList<String>();
			for (Tag tag : tags) {
				tagLabels.add(tag.getLabel());
			}
			
		} catch (SoundException e) {
			logger.error(e);
			return Response
					.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Cannot detach Tag because internal server error")
					.build();
		}
		
		return Response.status(Response.Status.OK).entity(tagLabels).build();
	}

	@GET
	@Path("/sounds/{label}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSoundsByTag(
			@NotNull @PathParam("label") String tagLabel) 
	{
		List<Sound> sounds = null;
		try {
			sounds = tagService.getSoundsWithTag(tagLabel);
		} catch (SoundException e) {
			logger.error(e);
			return Response
					.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Cannot get sounds by tag because internal server error")
					.build();
		}
		
		return Response.status(Response.Status.OK).entity(sounds.toString()).build();
	}

}

package com.sound.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.Tag;
import com.sound.service.sound.itf.SoundService;

@Component
@Path("/tag")
public class TagServiceEndpoint {

	@Autowired
	com.sound.service.sound.itf.TagService tagService;

	@Autowired
	SoundService soundService;

	@PUT
	@Path("/create/{tag}")
	public Response createTag(@PathParam("tag") String label) {
		if (StringUtils.isBlank(label))
		{
			return Response
					.status(Response.Status.BAD_REQUEST)
					.entity("Cannot Create Tag because input tag label is invalid")
					.build();
		}
		
		try 
		{
			tagService.getOrCreate(label);

		} catch (SoundException e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Cannot Create Tag because server internal error")
					.build();
		}
		
		return Response.status(Response.Status.CREATED)
				.entity("true").build();
	}

	@POST
	@Path("/attach")
	public Response attachTagsToSound(@FormParam("soundId") String soundId,
			@FormParam("tags") List<String> tagLabels) {
		if (StringUtils.isBlank(soundId))
		{
			return Response
					.status(Response.Status.BAD_REQUEST)
					.entity("Cannot attach Tag because input sound id is invalid")
					.build();
		}
		
		if (tagLabels == null || tagLabels.isEmpty())
		{
			return Response
					.status(Response.Status.BAD_REQUEST)
					.entity("Cannot attach Tag because input tag list is invalid")
					.build();
		}
		
		try 
		{
			tagService.attachToSound(soundId, tagLabels);
		} catch (Exception e) {
			e.printStackTrace();
			return Response
					.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Cannot attach Tag because internal server error")
					.build();
		}
		
		return Response.status(Response.Status.OK).entity("true").build();
	}

	@PUT
	@Path("/detach")
	public Response detachTagsFromSound(@FormParam("soundId") String soundId,
			@FormParam("tags") List<String> tagLabels) {
		if (StringUtils.isBlank(soundId))
		{
			return Response
					.status(Response.Status.BAD_REQUEST)
					.entity("Cannot detach Tag because input sound id is invalid")
					.build();
		}
		
		if (tagLabels == null || tagLabels.isEmpty())
		{
			return Response
						.status(Response.Status.BAD_REQUEST)
						.entity("Cannot detach Tag because input tag list is invalid")
						.build();
		}
		
		try 
		{
			tagService.detachFromSound(soundId, tagLabels);
		} 
		catch (Exception e) {
			e.printStackTrace();
			return Response
					.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Cannot detach Tag because internal server error")
					.build();
		}
		
		return Response.status(Response.Status.OK).entity("true").build();
	}

	@GET
	@Path("/match/{pattern}}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTagsContains(@PathParam("pattern") String pattern) {
		if (StringUtils.isBlank(pattern))
		{
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("Cannot get tags because pattern is invalid")
					.build();
		}
		
		List<String> tagLabels = null;
		try {
			List<Tag> tags = tagService.listTagsContains(pattern);
			tagLabels = new ArrayList<String>();
			for (Tag tag : tags) {
				tagLabels.add(tag.getLabel());
			}
			
		} catch (SoundException e) {
			e.printStackTrace();
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
	public Response getSoundsByTag(@PathParam("label") String tagLabel) {
		if (StringUtils.isBlank(tagLabel))
		{
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("Cannot get sounds by tag because tag is invalid")
					.build();
		}
		
		List<Sound> sounds = null;
		try {
			sounds = tagService.getSoundsWithTag(tagLabel);
		} catch (SoundException e) {
			e.printStackTrace();
			return Response
					.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Cannot get sounds by tag because internal server error")
					.build();
		}
		
		return Response.status(Response.Status.OK).entity(sounds.toString()).build();
	}

}

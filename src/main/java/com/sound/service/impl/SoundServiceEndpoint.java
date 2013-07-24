package com.sound.service.impl;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.service.file.itf.FileService;
import com.sound.service.sound.itf.SoundService;

@Component
@Path("/sound")
public class SoundServiceEndpoint {

	@Autowired
	FileService fileService;

	@Autowired
	SoundService soundService;

	@GET
	@Path("/{soundAlias}")
	public Response loadSound(
			@PathParam("soundAlias") String soundAlias
			)
	{
		Sound sound = null;
		try
		{
			sound = soundService.load(soundAlias);
		}
		catch (Exception e)
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to load sound " + soundAlias).build();
		}
		
		return Response.status(Status.OK).entity(sound.toString()).build();
	}
	
	@PUT
	@Path("/save")
	public Response saveProfile(
			@FormParam("objectId") String objectId, 
			@FormParam("soundAlias") String soundAlias, 
			@FormParam("description") String description, 
			@FormParam("ownerAlias") String ownerAlias, 
			@FormParam("status") String status,
			@FormParam("posterId") String posterId)
	{

		if (null == objectId)
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("sound object id can't be null").build(); 
		}

		if (null == soundAlias)
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("sound alias can't be null").build(); 
		}
		
		try
		{
			soundService.saveProfile(objectId, soundAlias, description, ownerAlias, status, posterId);
		}
		catch(SoundException e)
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		catch(Exception e)
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to save sound " + soundAlias).build();
		}
		return Response.status(Status.OK).entity("true").build();
	}

	@PUT
	@Path("/addToSet")
	public Response addToSet(@FormParam("userId") String userId,
			@FormParam("soundId") String soundId,
			@FormParam("SetId") String setId) {
		soundService.addToSet(soundId, setId);

		return Response.status(Status.OK).entity("true").build();
	}

	@DELETE
	@Path("/{soundAlias}")
	public Response delete(@PathParam("soundAlias") String soundAlias) {
		
		try
		{
			soundService.delete(soundAlias);
		}
		catch(Exception e)
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to delete sound " + soundAlias).build();
		}
		return Response.status(Status.OK).entity("true").build();
	}

}

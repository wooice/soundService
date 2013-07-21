package com.sound.service.impl;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
	@Produces("text/plain")
	public String loadSound(
			@PathParam("soundAlias") String soundAlias
			)
	{
		Sound sound = null;
		try
		{
			sound = soundService.load(soundAlias);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e.getMessage()); 
		}
		
		return sound.toString();
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
			return Response.status(500).entity("sound object id can't be null").build(); 
		}

		if (null == soundAlias)
		{
			return Response.status(500).entity("sound alias can't be null").build(); 
		}
		
		try
		{
			soundService.saveProfile(objectId, soundAlias, description, ownerAlias, status, posterId);
		}
		catch(Exception e)
		{
			return Response.status(500).entity(e.getMessage()).build();
		}
		return Response.status(200).entity("true").build();
	}

	@PUT
	@Path("/addToSet")
	public Response addToSet(@FormParam("userId") String userId,
			@FormParam("soundId") String soundId,
			@FormParam("SetId") String setId) {
		soundService.addToSet(soundId, setId);

		return null;
	}

	@DELETE
	@Path("/{soundId}")
	public Response delete(@PathParam("soundId") String soundId) {
		soundService.delete(soundId);

		return null;
	}

}

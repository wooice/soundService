package com.sound.service.impl;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.exception.SoundException;
import com.sound.service.sound.itf.SoundSocialService;

@Component
@Path("/soundActivity")
public class SoundSocialServiceEndpoint {

	@Autowired
	SoundSocialService soundSocialService;

	@PUT
	@Path("/like")
	public Response like(
			@FormParam("soundAlias") String soundAlias,
			@FormParam("soundAlias") String userAlias
			)
	{
		try {
			soundSocialService.like(soundAlias, userAlias);
		} catch (SoundException e) 
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(null).build();
		}
		catch (Exception e)
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to like sound " + soundAlias).build();
		}
		return Response.status(Status.OK).entity("true").build();
	}

	@DELETE
	@Path("/like")
	public Response unlike(
			@FormParam("soundAlias") String soundAlias,
			@FormParam("soundAlias") String userAlias
			)
	{
		try 
		{
			soundSocialService.unlike(soundAlias, userAlias);
		} catch (SoundException e) 
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(null).build();
		}
		catch (Exception e)
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to unlike sound " + soundAlias).build();
		}
		return Response.status(Status.OK).entity("true").build();
	}
}

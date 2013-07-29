package com.sound.service.impl;

import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.exception.SoundException;
import com.sound.exception.UserException;
import com.sound.service.sound.itf.SoundSocialService;

@Component
@Path("/soundActivity")
public class SoundSocialServiceEndpoint {

	Logger logger = Logger.getLogger(SoundSocialServiceEndpoint.class);
	
	@Autowired
	SoundSocialService soundSocialService;

	@PUT
	@Path("/{userAlias}/like/{soundAlias}")
	public Response like(
			@NotNull @PathParam("soundAlias") String soundAlias,
			@NotNull @PathParam("userAlias") String userAlias
			)
	{
		try {
			soundSocialService.like(soundAlias, userAlias);
		} catch (SoundException e) 
		{
			logger.error(e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		catch (Exception e)
		{
			logger.error(e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to like sound " + soundAlias).build();
		}
		return Response.status(Status.OK).entity("true").build();
	}

	@DELETE
	@Path("/{userAlias}/like/{soundAlias}")
	public Response unlike(
			@NotNull @PathParam("soundAlias") String soundAlias,
			@NotNull @PathParam("userAlias") String userAlias
			)
	{
		try 
		{
			soundSocialService.dislike(soundAlias, userAlias);
		} catch (SoundException e) 
		{
			logger.error(e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		catch (Exception e)
		{
			logger.error(e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to unlike sound " + soundAlias).build();
		}
		return Response.status(Status.OK).entity("true").build();
	}
	
	@PUT
	@Path("/{userAlias}/repost/{soundAlias}")
	public Response repost(
			@NotNull @PathParam("soundAlias") String soundAlias,
			@NotNull @PathParam("userAlias") String userAlias
			)
	{
		try {
			soundSocialService.repost(soundAlias, userAlias);
		} catch (SoundException e) 
		{
			logger.error(e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		catch (Exception e)
		{
			logger.error(e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to repost sound " + soundAlias).build();
		}
		return Response.status(Status.OK).entity("true").build();
	}

	@DELETE
	@Path("/{userAlias}/repost/{soundAlias}")
	public Response unrepost(
			@NotNull @PathParam("soundAlias") String soundAlias,
			@NotNull @PathParam("userAlias") String userAlias
			)
	{
		try 
		{
			soundSocialService.unrepost(soundAlias, userAlias);
		} catch (SoundException e) 
		{
			logger.error(e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		catch (Exception e)
		{
			logger.error(e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to unlike sound " + soundAlias).build();
		}
		return Response.status(Status.OK).entity("true").build();
	}
	
	@PUT
	@Path("/{userAlias}/comment/{soundAlias}")
	public Response comment(
			@NotNull @PathParam("soundAlias") String soundAlias,
			@NotNull @PathParam("userAlias") String userAlias,
			@NotNull @FormParam("comment")  String comment,
			@FormParam("pointAt") Float pointAt
			)
	{
		try {
			soundSocialService.comment(soundAlias, userAlias, comment, pointAt);
		}
		catch (UserException e) 
		{
			logger.error(e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		catch (SoundException e) 
		{
			logger.error(e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		catch (Exception e)
		{
			logger.error(e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to comment on sound " + soundAlias).build();
		}
		return Response.status(Status.OK).entity("true").build();
	}
	
	@DELETE
	@Path("/comment/{commentId}")
	public Response comment(
			@NotNull @PathParam("commentId") String commentId
			)
	{
		try {
			soundSocialService.uncomment(commentId);
		}
		catch (SoundException e) 
		{
			logger.error(e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		catch (Exception e)
		{
			logger.error(e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to comment with id " + commentId).build();
		}
		return Response.status(Status.OK).entity("true").build();
	}
}

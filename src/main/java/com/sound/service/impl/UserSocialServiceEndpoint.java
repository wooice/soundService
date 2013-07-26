package com.sound.service.impl;

import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.exception.UserException;
import com.sound.service.user.itf.UserSocialService;

@Component
@Path("/userSocial")
public class UserSocialServiceEndpoint {

	@Autowired
	UserSocialService userSocialService;
	
	@PUT
	@Path("/follow")
	public Response like(
			@FormParam("fromUserAlias") @NotNull String fromUserAlias,
			@FormParam("toUserAlias") @NotNull String toUserAlias
			)
	{
		try {
			userSocialService.follow(fromUserAlias, toUserAlias);
		} catch (UserException e) 
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		catch (Exception e)
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to follow user " + toUserAlias).build();
		}
		return Response.status(Status.OK).entity("true").build();
	}

	@DELETE
	@Path("/follow")
	public Response unfollow(
			@FormParam("fromUserAlias") @NotNull String fromUserAlias,
			@FormParam("toUserAlias") @NotNull String toUserAlias
			)
	{
		try 
		{
			userSocialService.unfollow(fromUserAlias, toUserAlias);
		} catch (UserException e) 
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		catch (Exception e)
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to follow user " + toUserAlias).build();
		}
		return Response.status(Status.OK).entity("true").build();
	}
}

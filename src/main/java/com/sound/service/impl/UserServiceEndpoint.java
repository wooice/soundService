package com.sound.service.impl;

import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.exception.UserException;
import com.sound.model.User;

@Component
@Path("/user")
public class UserServiceEndpoint{

	@Autowired
	com.sound.service.user.itf.UserService userService;

	@GET
	@Path("/{userAlias}/checkAlias")
	public Response checkAlias(
		@PathParam("userAlias") @NotNull String userAlias
	) 
	{
		User user = null;
		
		try
		{
			user = userService.getUserByAlias(userAlias);
		}catch(Exception e)
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to get user by alias " + userAlias).build();
		}
		String result = (null == user)? "true" : "false";

		return Response.status(Status.OK).entity(result).build();
	}

	@GET
	@Path("/{emailAddress}/checkEmail")
	public Response checkEmail(
		@PathParam("emailAddress") @NotNull String emailAddress
	) 
	{
		User user = null;
		try
		{
			user = userService.getUserByEmail(emailAddress);
		}
		catch(Exception e)
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to check emailaddress " + emailAddress).build();
		}
		String result = (null == user)? "true" : "false";

		return Response.status(Status.OK).entity(result).build();
	}

	@PUT
	@Path("/create")
	public Response create(
			@FormParam("userAlias") @NotNull String userAlias , 
			@FormParam("emailAddress") @NotNull String emailAddress,
			@FormParam("password") @NotNull String password
	)
	{
		try
		{
			userService.createUser(userAlias, emailAddress, password);
		}
		catch (UserException e) 
		{
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		catch(Exception e)
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to create user " + userAlias).build();
		}

		return Response.status(Status.OK).entity("true").build();
	}

}

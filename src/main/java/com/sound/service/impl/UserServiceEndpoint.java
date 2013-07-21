package com.sound.service.impl;

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
		@PathParam("userAlias") String userAlias
	) 
	{
		User user = userService.getUserByAlias(userAlias);
		String result = (null == user)? "true" : "false";

		return Response.status(Status.OK).entity(result).build();
	}

	@GET
	@Path("/{emailAddress}/checkEmail")
	public Response checkEmail(
		@PathParam("emailAddress") String emailAddress
	) 
	{
		User user = userService.getUserByEmail(emailAddress);
		String result = (null == user)? "true" : "false";

		return Response.status(Status.OK).entity(result).build();
	}

	@PUT
	@Path("/create")
	public Response create(
			@FormParam("userAlias") String userAlias , 
			@FormParam("emailAddress") String emailAddress,
			@FormParam("password") String password
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

		return Response.status(Status.OK).entity("true").build();
	}

}

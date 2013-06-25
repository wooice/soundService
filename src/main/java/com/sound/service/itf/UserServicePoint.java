package com.sound.service.itf;

import javax.ws.rs.core.Response;

public interface UserServicePoint {

	/**
	 * check if the user with the alias exists.
	 * 
	 * @param alias
	 */
	public Response checkAlias(String alias);

	public Response checkEmail(String emailAddress);
	
	public Response create(String emailAddress, String alias);

}

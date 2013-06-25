package com.sound.service.user.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.sound.dao.UserDAO;
import com.sound.exception.UserException;
import com.sound.model.User;

public class UserService implements com.sound.service.user.itf.UserService {

	@Autowired
	UserDAO userDAO;	

	@Override
	public User getUserByAlias(String userAlias)
	{
		User user = userDAO.findByAlias(userAlias);

		return user;
	}

	@Override
	public User getUserByEmail(String emailAddress) {
		User user = userDAO.findByEmail(emailAddress);

		return user;
	}

	@Override
	public User createUser(String userAlias, String emailAddress) throws UserException
	{
		User user = this.getUserByAlias(userAlias);

		if (null != user)
		{
			throw new UserException("User with alias " + userAlias + " exists.");
		}

		user = this.getUserByEmail(emailAddress);

		if (null != user)
		{
			throw new UserException("User with email address " + emailAddress + " exists.");
		}

		user = new User();
		User.UserProfile profile = new User.UserProfile();
		profile.setAlias(userAlias);
		User.UserEmail email = new User.UserEmail();
		email.setEmailAddress(emailAddress);
		user.setProfile(profile);
		user.addEmail(email);

		userDAO.save(user);

		return user;
	}

}

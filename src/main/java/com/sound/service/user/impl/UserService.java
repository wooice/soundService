package com.sound.service.user.impl;

import java.util.ArrayList;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sound.dao.UserDAO;
import com.sound.exception.UserException;
import com.sound.model.User;
import com.sound.model.UserAuth;
import com.sound.model.UserAuth.ChangeHistory;

@Service
@Scope("singleton")
public class UserService implements com.sound.service.user.itf.UserService {

	@Autowired
	UserDAO userDAO;	

	@Override
	public User getUserByAlias(String userAlias)
	{
		User user = userDAO.findOne("profile.alias", userAlias);

		return user;
	}

	@Override
	public User getUserByEmail(String emailAddress) {
		User user = userDAO.findOne("emails.emailAddress", emailAddress);

		return user;
	}

	@Override
	public User createUser(String userAlias, String emailAddress, String password) throws UserException
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
		profile.setPassword(password);
		User.UserEmail email = new User.UserEmail();
		email.setEmailAddress(emailAddress);
		user.setProfile(profile);
		user.addEmail(email);

		userDAO.save(user);

		return user;
	}

	@Override
	public User updatePassword(String emailAddress, String password, String ip) throws UserException{
		User user =  this.getUserByEmail(emailAddress);

		if (null == user)
		{
			throw new UserException("Cannot find user with email address : " + emailAddress);
		}
		
		UserAuth auth = user.getAuth();
		if (auth == null) {
			auth = new UserAuth();
			user.setAuth(auth);
			auth.setHisoties(new ArrayList<ChangeHistory>());
		}
		auth.setId(user.getId());
		auth.setPassword(password);
		ChangeHistory history = new ChangeHistory();
		history.setIp(ip);
		history.setModifiedDate(new Date());
		history.setPassword(password);
		auth.getHisoties().add(history);
		
		userDAO.save(user);
		
		return user;
	}

	public void deleteByAlias(String userAlias)
	{
		userDAO.deleteByProperty("profile.alias", userAlias);
	}

	public UserDAO getUserDAO() {
		return userDAO;
	}

	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}
	
}

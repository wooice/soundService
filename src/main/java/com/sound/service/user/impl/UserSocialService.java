package com.sound.service.user.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.sound.dao.UserConnectDAO;
import com.sound.dao.UserDAO;
import com.sound.exception.UserException;
import com.sound.model.User;
import com.sound.model.UserActivity.UserConnect;

public class UserSocialService implements
		com.sound.service.user.itf.UserSocialService {

	@Autowired
	UserDAO userDAO;	
	
	@Autowired
	UserConnectDAO userConnectDAO;
	
	@Override
	public void follow(String fromUserAlias, String toUserAlias)
			throws UserException {
		Map<String, String> cratiaries = new HashMap<String, String>();
		cratiaries.put("fromUser.profile.alias", fromUserAlias);
		cratiaries.put("toUser.profile.alias", toUserAlias);
		UserConnect userConnected = userConnectDAO.findOne(cratiaries);
		
		if (userConnected != null)
		{
			throw new UserException("The user " + fromUserAlias + "has followed user " + toUserAlias);
		}
		
		User fromUser = userDAO.findOne("profile.alias", fromUserAlias);
		if (null == fromUser)
		{
			throw new UserException("The user " + fromUserAlias + "not found.");
		}
		
		User toUser = userDAO.findOne("profile.alias", toUserAlias);
		if (null == toUser)
		{
			throw new UserException("The user " + toUserAlias + "not found.");
		}
		
		UserConnect userConnect = new UserConnect();
		userConnect.setFromUser(fromUser);
		userConnect.setToUser(toUser);
		userConnect.setCreatedTime(new Date());
		
		userConnectDAO.save(userConnect);
		
		userDAO.increase("profile.alias", fromUserAlias, "following");
		userDAO.increase("profile.alias", toUserAlias, "followed");
	}

	@Override
	public void unfollow(String fromUserAlias, String toUserAlias)
			throws UserException {
		Map<String, String> cratiaries = new HashMap<String, String>();
		cratiaries.put("fromUser.profile.alias", fromUserAlias);
		cratiaries.put("toUser.profile.alias", toUserAlias);
		UserConnect userConnected = userConnectDAO.findOne(cratiaries);
		
		if (userConnected == null)
		{
			throw new UserException("The user " + fromUserAlias + "hasn't followed user " + toUserAlias);
		}
		
		userConnectDAO.delete(userConnected);
		
		userDAO.decrease("profile.alias", fromUserAlias, "following");
		userDAO.decrease("profile.alias", toUserAlias, "followed");
	}

	public UserConnectDAO getUserConnectDAO() {
		return userConnectDAO;
	}

	public void setUserConnectDAO(UserConnectDAO userConnectDAO) {
		this.userConnectDAO = userConnectDAO;
	}

	public UserDAO getUserDAO() {
		return userDAO;
	}

	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}

}

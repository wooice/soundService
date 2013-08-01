package com.sound.service.user.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sound.dao.GroupDAO;
import com.sound.dao.UserConnectDAO;
import com.sound.dao.UserDAO;
import com.sound.exception.UserException;
import com.sound.model.Group;
import com.sound.model.User;
import com.sound.model.UserActivity.UserConnect;

@Service
@Scope("singleton")
public class UserSocialService implements
		com.sound.service.user.itf.UserSocialService {

	@Autowired
	UserDAO userDAO;	
	
	@Autowired
	UserConnectDAO userConnectDAO;
	
	@Autowired
	GroupDAO groupDAO;
	
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

	@Override
	public void createGroup(String userAlias, String groupName, String description)
			throws UserException {
		User user = userDAO.findOne("profile.alias", userAlias);
		
		if (null == user)
		{
			throw new UserException("User " + userAlias + " not found.");
		}
		Group group = new Group();
		group.setName(groupName);
		group.setDescription(description);
		group.setOwner(user);
		groupDAO.save(group);
		
		user.addGroup(group);
		userDAO.updateProperty("profile.alias", userAlias, "groups", user.getGroups());
	}

	@Override
	public void dismissGroup(String userAlias, String groupName)
			throws UserException {
		User user = userDAO.findOne("profile.alias", userAlias);
		
		if (null == user)
		{
			throw new UserException("User " + userAlias + " not found.");
		}
		
		Group group = groupDAO.findOne("name", groupName);
		if (group.getOwner().getProfile().getAlias() == userAlias)
		{
			List<User> users = userDAO.find("group.name", groupName);
			for(User groupUser: users)
			{
				groupUser.removeGroup(group);
				userDAO.updateProperty("profile.alias", groupUser.getProfile().getAlias(), "groups", groupUser.getGroups());
			}
			
			groupDAO.delete(group);
		}
		else
		{
			throw new UserException("User " + userAlias + " don't have rights to delete the group " + groupName);
		}
	}

	@Override
	public void joinGroup(String userAlias, String groupName) throws UserException {
		User user = userDAO.findOne("profile.alias", userAlias);
		
		if (null == user)
		{
			throw new UserException("User " + userAlias + " not found.");
		}
		
		Group group = groupDAO.findOne("name", groupName);
		user.addGroup(group);
		userDAO.updateProperty("profile.alias", userAlias, "groups", user.getGroups());
	}

	@Override
	public void leaveGroup(String userAlias, String groupName) throws UserException {
		User user = userDAO.findOne("profile.alias", userAlias);
		
		if (null == user)
		{
			throw new UserException("User " + userAlias + " not found.");
		}
		
		Group group = groupDAO.findOne("name", groupName);
		user.removeGroup(group);
		userDAO.updateProperty("profile.alias", userAlias, "groups", user.getGroups());
	}

	@Override
	public void promoteGroupAdmin(String ownerAlias, String adminAlias,
			String groupName) throws UserException {
		User owner = userDAO.findOne("profile.alias", ownerAlias);
		if (null == owner)
		{
			throw new UserException("User " + ownerAlias + " not found.");
		}
		
		User admin = userDAO.findOne("profile.alias", adminAlias);
		if (null == admin)
		{
			throw new UserException("User " + adminAlias + " not found.");
		}
		
		Group group = groupDAO.findOne("name", groupName);
		if (group.getOwner().getProfile().getAlias() == ownerAlias)
		{
			group.addAdmin(admin);
			groupDAO.updateProperty("name", groupName, "admins", group.getAdmins());
		}
		else
		{
			throw new UserException("User " + ownerAlias + " don't have rights to promote admin of the group " + groupName);
		}
	}

	@Override
	public void demoteGroupAdmin(String ownerAlias, String adminAlias,
			String groupName) throws UserException {
		User owner = userDAO.findOne("profile.alias", ownerAlias);
		if (null == owner)
		{
			throw new UserException("User " + ownerAlias + " not found.");
		}
		
		User admin = userDAO.findOne("profile.alias", adminAlias);
		if (null == admin)
		{
			throw new UserException("User " + adminAlias + " not found.");
		}
		
		Group group = groupDAO.findOne("name", groupName);
		if (group.getOwner().getProfile().getAlias() == ownerAlias)
		{
			group.removeAdmin(admin);
			groupDAO.updateProperty("name", groupName, "admins", group.getAdmins());
		}
		else
		{
			throw new UserException("User " + ownerAlias + " don't have rights to promote admin of the group " + groupName);
		}
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

	public GroupDAO getGroupDAO() {
		return groupDAO;
	}

	public void setGroupDAO(GroupDAO groupDAO) {
		this.groupDAO = groupDAO;
	}

}

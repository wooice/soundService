package com.sound.service.user.itf;

import java.util.List;

import com.sound.exception.SoundException;
import com.sound.exception.UserException;
import com.sound.model.Group;
import com.sound.model.User;

public interface UserSocialService 
{
	public void follow(String fromUserAlias, String toUserAlias) throws UserException;
	
	public void unfollow(String fromUserAlias, String toUserAlias) throws UserException;
	
	public void createGroup(String user, String groupName, String description)  throws UserException;
	
	public void dismissGroup(String user, String groupName) throws UserException;
	
	public void joinGroup(String user, String groupName) throws UserException;
	
	public void leaveGroup(String user, String groupName) throws UserException;
	
	public void promoteGroupAdmin(String ownerAlias, String adminAlias, String groupName) throws UserException;
	
	public void demoteGroupAdmin(String ownerAlias, String adminAlias, String groupName) throws UserException;
	
	public List<User> getFollowedUsers(String toUserAlias) throws UserException;
	
	public List<User> getFollowingUsers(String fromUserAlias) throws UserException;

	public List<User> recommandUsersByTags(List<String> tagLabels) throws UserException,SoundException;

	public List<Group> recommandGroupsByTags(List<String> tagLabels) throws UserException, SoundException;
}

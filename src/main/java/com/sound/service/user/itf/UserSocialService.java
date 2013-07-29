package com.sound.service.user.itf;

import com.sound.exception.UserException;

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
}

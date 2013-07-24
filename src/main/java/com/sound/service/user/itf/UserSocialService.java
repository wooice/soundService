package com.sound.service.user.itf;

import com.sound.exception.UserException;

public interface UserSocialService 
{
	public void follow(String fromUserAlias, String toUserAlias) throws UserException;
	
	public void unfollow(String fromUserAlias, String toUserAlias) throws UserException;
}

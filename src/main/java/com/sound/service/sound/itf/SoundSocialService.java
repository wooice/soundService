package com.sound.service.sound.itf;

import com.sound.exception.SoundException;
import com.sound.exception.UserException;

public interface SoundSocialService {

	public Integer like(String soundAlias, String userAlias)  throws SoundException;
	
	public Integer dislike(String soundAlias, String userAlias)  throws SoundException;
	
	public void repost(String soundAlias, String userAlias)  throws SoundException;
	
	public void unrepost(String soundAlias, String userAlias)  throws SoundException;
	
	public void comment(String soundAlias, String userAlias, String comment, Float pointAt) throws SoundException, UserException;
	
	public void uncomment(String commentId) throws SoundException;
}

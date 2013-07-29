package com.sound.service.sound.itf;

import com.sound.exception.SoundException;
import com.sound.exception.UserException;

public interface SoundSocialService {

	public Integer like(String soundAlias, String userAlias)  throws SoundException;
	
	public Integer unlike(String soundAlias, String userAlias)  throws SoundException;
	
	public void report(String soundAlias, String userAlias)  throws SoundException;
	
	public void unReport(String soundAlias, String userAlias)  throws SoundException;
	
	public void comment(String soundAlias, String userAlias, String comment, Float pointAt) throws SoundException, UserException;
	
	public void unComment(String commentId) throws SoundException;
}

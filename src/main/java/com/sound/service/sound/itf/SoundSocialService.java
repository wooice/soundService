package com.sound.service.sound.itf;

import com.sound.exception.SoundException;

public interface SoundSocialService {

	public Integer like(String soundAlias, String userAlias)  throws SoundException;
	
	public Integer unlike(String soundAlias, String userAlias)  throws SoundException;
	
}

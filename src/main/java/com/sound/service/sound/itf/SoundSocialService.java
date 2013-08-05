package com.sound.service.sound.itf;

import java.util.List;

import com.sound.exception.SoundException;
import com.sound.exception.UserException;
import com.sound.model.Sound;

public interface SoundSocialService {

	public Integer play(String soundAlias, String userAlias)  throws SoundException;
	
	public Integer like(String soundAlias, String userAlias)  throws SoundException;
	
	public Integer dislike(String soundAlias, String userAlias)  throws SoundException;
	
	public void repost(String soundAlias, String userAlias)  throws SoundException;
	
	public void unrepost(String soundAlias, String userAlias)  throws SoundException;
	
	public void comment(String soundAlias, String userAlias, String comment, Float pointAt) throws SoundException, UserException;
	
	public void uncomment(String commentId) throws SoundException;

	public List<Sound> recommandSoundsByTags(List<String> tagLabels, Integer pageNum, Integer pageSize) throws SoundException;
}

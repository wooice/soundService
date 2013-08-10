package com.sound.service.sound.itf;

import java.util.List;

import com.sound.exception.SoundException;
import com.sound.exception.UserException;
import com.sound.model.Sound;
import com.sound.model.SoundActivity.SoundComment;

public interface SoundSocialService {

	public Integer play(String soundAlias, String userAlias)  throws SoundException;
	
	public Integer like(String soundAlias, String userAlias)  throws SoundException;
	
	public Integer dislike(String soundAlias, String userAlias)  throws SoundException;
	
	public Integer repost(String soundAlias, String userAlias)  throws SoundException;
	
	public Integer unrepost(String soundAlias, String userAlias)  throws SoundException;
	
	public Integer comment(String soundAlias, String userAlias, String comment, Float pointAt) throws SoundException, UserException;
	
	public Integer uncomment(String commentId) throws SoundException;

	public List<Sound> recommandSoundsByTags(List<String> tagLabels, Integer pageNum, Integer pageSize) throws SoundException;

	public List<SoundComment> getComments(String soundAlias, Integer pageNum, Integer soundsPerPage) throws SoundException;
}

package com.sound.service.sound.itf;

import java.util.List;
import java.util.Map;

import com.sound.exception.SoundException;
import com.sound.exception.UserException;
import com.sound.model.Sound;
import com.sound.model.SoundActivity.SoundComment;
import com.sound.model.User;

public interface SoundSocialService {

	public Map<String, String> play(String soundAlias, User user)  throws SoundException;
	
	public Integer like(String soundAlias, User user)  throws SoundException;
	
	public Integer dislike(String soundAlias, User user)  throws SoundException;
	
	public Integer repost(String soundAlias, User user)  throws SoundException;
	
	public Integer unrepost(String soundAlias, User user)  throws SoundException;
	
	public Integer comment(String soundAlias, User user, String comment, Float pointAt) throws SoundException, UserException;
	
	public Integer uncomment(String commentId) throws SoundException;

	public List<Sound> recommandSoundsByTags(List<String> tagLabels, Integer pageNum, Integer pageSize) throws SoundException;

	public List<Sound> getLikedSoundsByUser(User user) throws SoundException;

	public List<Sound> recommandSoundsForUser(User user, Integer pageNum, Integer pageSize) throws SoundException, UserException;

	public List<SoundComment> getComments(String soundAlias, Integer pageNum, Integer soundsPerPage) throws SoundException;

}

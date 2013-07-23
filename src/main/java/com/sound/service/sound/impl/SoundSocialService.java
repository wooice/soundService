package com.sound.service.sound.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.sound.dao.SoundDAO;
import com.sound.dao.SoundLikeDAO;
import com.sound.dao.SoundRepostDAO;
import com.sound.dao.UserDAO;
import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.SoundActivity.SoundLike;

public class SoundSocialService implements com.sound.service.sound.itf.SoundSocialService{

	@Autowired
	UserDAO userDAO;

	@Autowired
	SoundDAO soundDAO;
	
	@Autowired
	SoundLikeDAO soundLikeDAO;
	
	@Autowired
	SoundRepostDAO soundRepostDAO;
	
	@Override
	public Integer like(String soundAlias, String userAlias) throws SoundException {
		Map<String, String> cratiaries = new HashMap<String, String>();
		cratiaries.put("sound.profile.name", soundAlias);
		cratiaries.put("user.profile.alias", userAlias);
		SoundLike liked = soundLikeDAO.findOne(cratiaries);
		
		if (null != liked)
		{
			throw new SoundException("The user " + userAlias +" has liked sound " + soundAlias);
		}
		
		SoundLike like = new SoundLike();
		Sound sound = soundDAO.findOne("profile.name", soundAlias);
		like.setSound(sound);
		like.setOwner(userDAO.findOne("profile.alias", userAlias));
		like.setCreatedTime(new Date());
		soundLikeDAO.save(like);
		soundDAO.increase("profile.name", soundAlias, "likesCount");
		
		return sound.getSoundSocial().getLikesCount() - 1;
	}

	@Override
	public Integer unlike(String soundAlias, String userAlias) throws SoundException 
	{
		Map<String, String> cratiaries = new HashMap<String, String>();
		cratiaries.put("sound.profile.name", soundAlias);
		cratiaries.put("user.profile.alias", userAlias);
		SoundLike liked = soundLikeDAO.findOne(cratiaries);
		
		if (null == liked)
		{
			throw new SoundException("The user " + userAlias +" hasn't liked sound " + soundAlias);
		}
		
		soundLikeDAO.delete(liked);
		soundDAO.decrease("profile.name", soundAlias, "likesCount");
		
		return soundDAO.findOne("profile.name", soundAlias).getSoundSocial().getLikesCount() - 1;
	}

	public SoundDAO getSoundDAO() {
		return soundDAO;
	}

	public void setSoundDAO(SoundDAO soundDAO) {
		this.soundDAO = soundDAO;
	}

	public UserDAO getUserDAO() {
		return userDAO;
	}

	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}

	public SoundLikeDAO getSoundLikeDAO() {
		return soundLikeDAO;
	}

	public void setSoundLikeDAO(SoundLikeDAO soundLikeDAO) {
		this.soundLikeDAO = soundLikeDAO;
	}

	public SoundRepostDAO getSoundRepostDAO() {
		return soundRepostDAO;
	}

	public void setSoundRepostDAO(SoundRepostDAO soundRepostDAO) {
		this.soundRepostDAO = soundRepostDAO;
	}

}

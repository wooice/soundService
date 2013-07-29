package com.sound.service.sound.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import com.sound.dao.SoundCommentDAO;
import com.sound.dao.SoundDAO;
import com.sound.dao.SoundLikeDAO;
import com.sound.dao.SoundRecordDAO;
import com.sound.dao.UserDAO;
import com.sound.exception.SoundException;
import com.sound.exception.UserException;
import com.sound.model.Sound;
import com.sound.model.SoundActivity.SoundComment;
import com.sound.model.SoundActivity.SoundLike;
import com.sound.model.SoundActivity.SoundRecord;
import com.sound.model.User;

public class SoundSocialService implements com.sound.service.sound.itf.SoundSocialService{

	@Autowired
	UserDAO userDAO;

	@Autowired
	SoundDAO soundDAO;
	
	@Autowired
	SoundLikeDAO soundLikeDAO;
	
	@Autowired
	SoundRecordDAO soundRecordDAO;
	
	@Autowired
	SoundCommentDAO soundCommentDAO;
	
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
	public Integer dislike(String soundAlias, String userAlias) throws SoundException 
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

	@Override
	public void repost(String soundAlias, String userAlias)
			throws SoundException {
		Map<String, String> cratiaries = new HashMap<String, String>();
		cratiaries.put("sound.profile.name", soundAlias);
		cratiaries.put("user.profile.alias", userAlias);
		cratiaries.put("recordType", SoundRecord.REPOST);
		SoundRecord reposted = soundRecordDAO.findOne(cratiaries);
		
		if (null != reposted)
		{
			throw new SoundException("The user " + userAlias +" has reposted sound " + soundAlias);
		}
		
		SoundRecord repost = new SoundRecord();
		Sound sound = soundDAO.findOne("profile.name", soundAlias);
		repost.setSound(sound);
		repost.setOwner(userDAO.findOne("profile.alias", userAlias));
		repost.setCreatedTime(new Date());
		soundRecordDAO.save(repost);
		soundDAO.increase("profile.name", soundAlias, "reportsCount");
	}

	@Override
	public void unrepost(String soundAlias, String userAlias)
			throws SoundException {
		Map<String, String> cratiaries = new HashMap<String, String>();
		cratiaries.put("sound.profile.name", soundAlias);
		cratiaries.put("user.profile.alias", userAlias);
		cratiaries.put("recordType", SoundRecord.REPOST);
		SoundRecord reposted = soundRecordDAO.findOne(cratiaries);
		
		if (null == reposted)
		{
			throw new SoundException("The user " + userAlias +" hasn't reposted sound " + soundAlias);
		}
		
		soundRecordDAO.delete(reposted);
		soundDAO.decrease("profile.name", soundAlias, "reportsCount");
	}
	
	@Override
	public void comment(String soundAlias, String userAlias, String comment,  Float pointAt)
			throws SoundException, UserException {
		SoundComment soundComment = new SoundComment();
		
		User user = userDAO.findOne("profile.name", userAlias);
		if(null == user)
		{
			throw new UserException("User " + userAlias + " not found.");
		}
		soundComment.setOwner(user);
		
		Sound sound = soundDAO.findOne("profile.name", soundAlias);
		if (null == sound)
		{
			throw new SoundException("Sound " + soundAlias + " not found.");
		}
		soundComment.setSound(sound);
		soundComment.setCreatedTime(new Date());
		soundComment.setPointAt(pointAt);
		
		soundCommentDAO.save(soundComment);
		soundDAO.increase("profile.name", soundAlias, "commentsCount");
	}
	
	@Override
	public void uncomment(String commentId) throws SoundException {
		SoundComment soundComment = soundCommentDAO.findOne("id", new ObjectId(commentId));
		
		if (null == soundComment)
		{
			throw new SoundException("Sound comment doesn't exist");
		}
		String soundAlias = soundComment.getSound().getProfile().getName();
		soundCommentDAO.delete(soundComment);
		soundDAO.decrease("profile.name", soundAlias, "commentsCount");
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

	public SoundRecordDAO getSoundRecordDAO() {
		return soundRecordDAO;
	}

	public void setSoundRecordDAO(SoundRecordDAO soundRecordDAO) {
		this.soundRecordDAO = soundRecordDAO;
	}

	public SoundCommentDAO getSoundCommentDAO() {
		return soundCommentDAO;
	}

	public void setSoundCommentDAO(SoundCommentDAO soundCommentDAO) {
		this.soundCommentDAO = soundCommentDAO;
	}

}

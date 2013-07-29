package com.sound.service.sound.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.sound.dao.SoundDAO;
import com.sound.dao.SoundRecordDAO;
import com.sound.dao.UserConnectDAO;
import com.sound.dao.UserDAO;
import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.Sound.SoundData;
import com.sound.model.Sound.SoundProfile;
import com.sound.model.Sound.SoundProfile.SoundPoster;
import com.sound.model.SoundActivity.SoundRecord;
import com.sound.model.User;
import com.sound.model.UserActivity.UserConnect;
import com.sound.model.enums.SoundType;
import com.sound.model.file.LocalSoundFile;
import com.sound.processor.factory.ProcessorFactory;
import com.sound.processor.itf.Converter;
import com.sound.processor.itf.Extractor;
import com.sound.processor.model.AudioInfo;
import com.sound.processor.model.Wave;

public class SoundService implements com.sound.service.sound.itf.SoundService
{
	@Autowired
	SoundDAO soundDAO;

	@Autowired
	UserDAO userDAO;	
	
	@Autowired
	UserConnectDAO userConnectDAO;
	
	@Autowired
	SoundDataService soundDataService;
	
	@Autowired
	SoundRecordDAO soundRecordDAO;

	@Autowired
	ProcessorFactory processFactory;

	@Override
	public LocalSoundFile uniform(LocalSoundFile sound) throws SoundException {
		File intermediateSound = null;
		File intermediateMp3Sound = null;
		File intermediateWavSound = null;
		try
		{
			intermediateSound = File.createTempFile("audio", sound.getType());
			FileUtils.writeByteArrayToFile(intermediateSound, sound.getContent());
			
			Converter mp3Converter = processFactory.getConverter("mp3");
			intermediateMp3Sound = mp3Converter.convert(intermediateSound);
			byte[] content = FileUtils.readFileToByteArray(intermediateMp3Sound);
			sound.setContent(content);
			sound.setType("mp3");

			Converter wavConverter = processFactory.getConverter("wav");
			intermediateWavSound = wavConverter.convert(intermediateSound);

			Extractor wavExtractor = processFactory.getExtractor("wav");
			Wave wave = wavExtractor.extractWaveByTotal(intermediateWavSound, 1800);
			sound.setWaveData(wave.getWaveData());

			AudioInfo audioInfo = wavExtractor.extractInfo(intermediateMp3Sound);
			sound.setDuration((int) audioInfo.getDuration() / 1000);

			return sound;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new SoundException(e.getMessage());
		}
		finally
		{
			if (null != intermediateSound)
			{
				intermediateSound.deleteOnExit();
			}

			if (null != intermediateMp3Sound)
			{
				intermediateMp3Sound.deleteOnExit();
			}

			if (null != intermediateWavSound)
			{
				intermediateWavSound.deleteOnExit();
			}
		}
	}

	@Override
	public void addToSet(String soundId, String setId) {
	}

	@Override
	public void delete(String soundAlias) {
		soundDAO.deleteByProperty("profile.name", soundAlias);
		//Delete create sound activity.
		soundRecordDAO.deleteByProperty("sound.profile.name", soundAlias);
	}

	@Override
	public Sound load(String soundAlias) {
		return soundDAO.findOne("profile.name", soundAlias);
	}

	@Override
	public void saveProfile(String remoteId, String soundAlias, String description, String ownerAlias, String status, String posterId) throws SoundException 
	{
		Sound sound = new Sound();
		
		SoundData soundData = soundDataService.load(remoteId);
		sound.setSoundData(soundData);
		
		SoundProfile soundProfile = new SoundProfile();
		
		soundProfile.setCreatedTime(new Date());
		soundProfile.setDownloadable(false);
		soundProfile.setModifiedTime(new Date());
		soundProfile.setName(soundAlias);
		soundProfile.setDescription(description);
		soundProfile.setPlayed(0);
		soundProfile.setStatus(status);
		soundProfile.setType(SoundType.SOUND.getTypeName());
		
		User owner = userDAO.findOne("profile.alias", ownerAlias);
		soundProfile.setOwner(owner);
		
		SoundPoster soundPoster = new SoundPoster();
		soundPoster.setPosterId(posterId);
		soundProfile.setPoster(soundPoster);
		
		sound.setProfile(soundProfile);
		
		soundDAO.save(sound);
		
		//Add activity for create new sound.
		SoundRecord soundRecord = new SoundRecord();
		soundRecord.setOwner(owner);
		soundRecord.setRecordType(SoundRecord.CREATE);
		soundRecord.setSound(sound);
		soundRecord.setCreatedTime(new Date());
		soundRecordDAO.save(soundRecord);
	}
	
	@Override
	public List<SoundRecord> getSoundsByUser(String userAlias, Integer pageNum, Integer soundsPerPage)
	{
		Map<String, String> cratiaries = new HashMap<String, String>();
		cratiaries.put("owner.profile.alias", userAlias);
		return soundRecordDAO.findWithRange(cratiaries, pageNum * soundsPerPage, soundsPerPage);
	}


	@Override
	public List<SoundRecord> getObservingSounds(String userAlias,
			Integer pageNum, Integer soundsPerPage) throws SoundException {
		List<UserConnect> connections = userConnectDAO.find("fromUser.profile.alias", userAlias);
		List<User> users = new ArrayList<User>();
		
		for(UserConnect connect : connections)
		{
			users.add(connect.getFromUser());
		}
		
		Map<String, String> cratiaries = Collections.emptyMap();
		soundRecordDAO.findByOwners(cratiaries, users, pageNum * soundsPerPage, soundsPerPage);
		return null;
	}

	// ---------setters & getters ----------
	
	public SoundDAO getSoundDAO() {
		return soundDAO;
	}

	public void setSoundDAO(SoundDAO soundDAO) {
		this.soundDAO = soundDAO;
	}

	public SoundRecordDAO getSoundRecordDAO() {
		return soundRecordDAO;
	}

	public void setSoundRecordDAO(SoundRecordDAO soundRecordDAO) {
		this.soundRecordDAO = soundRecordDAO;
	}

	public UserDAO getUserDAO() {
		return userDAO;
	}

	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}

	public ProcessorFactory getProcessFactory() {
		return processFactory;
	}

	public void setProcessFactory(ProcessorFactory processFactory) {
		this.processFactory = processFactory;
	}

	public SoundDataService getSoundDataService() {
		return soundDataService;
	}

	public void setSoundDataService(SoundDataService soundDataService) {
		this.soundDataService = soundDataService;
	}

	public UserConnectDAO getUserConnectDAO() {
		return userConnectDAO;
	}

	public void setUserConnectDAO(UserConnectDAO userConnectDAO) {
		this.userConnectDAO = userConnectDAO;
	}

}

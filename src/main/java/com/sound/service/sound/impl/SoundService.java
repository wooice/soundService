package com.sound.service.sound.impl;

import java.io.File;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.sound.dao.SoundDAO;
import com.sound.dao.UserDAO;
import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.Sound.SoundData;
import com.sound.model.Sound.SoundProfile;
import com.sound.model.Sound.SoundProfile.SoundPoster;
import com.sound.model.User;
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
	SoundDataService soundDataService;

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
	}

	@Override
	public Sound load(String soundAlias) {
		Sound sound = soundDAO.findOne("profile.name", soundAlias);
		return sound;
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
	}

	// ---------setters & getters ----------
	
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

}

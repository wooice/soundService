package com.sound.service.sound.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.sound.dao.SoundDAO;
import com.sound.dao.SoundDataDAO;
import com.sound.dao.UserDAO;
import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.Sound.SoundData;
import com.sound.model.Sound.SoundProfile;
import com.sound.model.Sound.SoundProfile.SoundPoster;
import com.sound.model.SoundSocial;
import com.sound.model.SoundSocial.SoundComment;
import com.sound.model.SoundSocial.SoundLike;
import com.sound.model.SoundSocial.SoundRepost;
import com.sound.model.User;
import com.sound.model.enums.SoundState;
import com.sound.model.enums.SoundType;
import com.sound.model.file.LocalFile;
import com.sound.model.file.LocalSoundFile;
import com.sound.model.file.RemoteFile;
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
	SoundDataDAO soundDataDAO;

	@Autowired
	ProcessorFactory processFactory;

	@Override
	public void save(LocalSoundFile soundFile, RemoteFile remoteFile) {
		User owner = userDAO.findByAlias(soundFile.getOwnerId());

		Sound sound = new Sound();
		Sound.SoundProfile soundProfile = new Sound.SoundProfile();
		soundProfile.setName(soundFile.getFileName());
		soundProfile.setCreatedTime(new Date());
		soundProfile.setModifiedTime(new Date());
		soundProfile.setOwner(owner);
		soundProfile.setPlayed(0);
		soundProfile.setPoster(null);
		soundProfile.setType(SoundType.getTypeId(soundFile.getType()));
		soundProfile.setStatus(SoundState.getStateId(soundFile.getStatus()));
		sound.setProfile(soundProfile);

		Sound.SoundData soundData = new Sound.SoundData();
		soundData.setWave(soundFile.getWaveData());
		soundData.setObjectId(remoteFile.getRemoteKey());
		soundData.setFileAlias(soundFile.getFileName());
		sound.setSoundData(soundData);

		SoundSocial soundSocial = new SoundSocial();
		soundSocial.setComments(new ArrayList<SoundComment>());
		soundSocial.setLikes(new ArrayList<SoundLike>());
		soundSocial.setReposts(new ArrayList<SoundRepost>());
		sound.setSoundSocial(soundSocial);

		soundDAO.save(sound);
	}

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
	public void delete(String soundId) {
	}

	@Override
	public Sound load(String soundId) {
		return null;
	}

	@Override
	public void saveProfile(String remoteId, String soundAlias, String description, String ownerAlias, String status, LocalFile poster) throws SoundException 
	{
		Sound sound = new Sound();
		
		SoundData soundData = soundDataDAO.findOneByRemoteId(remoteId);
		sound.setSoundData(soundData);
		
		SoundProfile soundProfile = new SoundProfile();
		
		soundProfile.setCreatedTime(new Date());
		soundProfile.setDownloadable(false);
		soundProfile.setModifiedTime(new Date());
		soundProfile.setName(soundAlias);
		soundProfile.setDescription(description);
		soundProfile.setPlayed(0);
		soundProfile.setStatus(SoundState.getStateId(status));
		soundProfile.setType(SoundType.SOUND.getTypeId());
		
		User owner = userDAO.findByAlias(ownerAlias);
		soundProfile.setOwner(owner);
		
		SoundPoster soundPoster = new SoundPoster();
		soundPoster.setExtension(poster.getType());
		soundPoster.setPoster(poster.getContent());
		soundProfile.setPoster(soundPoster);
		
		sound.setProfile(soundProfile);
		
		soundDAO.save(sound);
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

	public SoundDataDAO getSoundDataDAO() {
		return soundDataDAO;
	}

	public void setSoundDataDAO(SoundDataDAO soundDataDAO) {
		this.soundDataDAO = soundDataDAO;
	}

	public ProcessorFactory getProcessFactory() {
		return processFactory;
	}

	public void setProcessFactory(ProcessorFactory processFactory) {
		this.processFactory = processFactory;
	}

}

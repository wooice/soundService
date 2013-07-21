package com.sound.service.sound.impl;

import org.springframework.beans.factory.annotation.Autowired;

import com.sound.model.Sound.SoundData;
import com.sound.dao.SoundDataDAO;

public class SoundDataService implements com.sound.service.sound.itf.SoundDataService
{
	@Autowired
	SoundDataDAO soundDataDAO;

	public void save(String objectId, Integer duration, float[][] waveData)
	{
		SoundData soundData = new SoundData();
		soundData.setObjectId(objectId);
		soundData.setDuration(duration);
		soundData.setWave(waveData);
		
		soundDataDAO.save(soundData);
	}
	
	public SoundData load(String objectId)
	{
		return soundDataDAO.findOne("objectId", objectId);
	}
	
	public void delete(String objectId)
	{
		soundDataDAO.deleteByProperty("objectId", objectId);
	}
	
	public SoundDataDAO getSoundDataDAO() {
		return soundDataDAO;
	}

	public void setSoundDataDAO(SoundDataDAO soundDataDAO) {
		this.soundDataDAO = soundDataDAO;
	}

}

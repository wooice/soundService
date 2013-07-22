package com.sound.service.sound.itf;

import com.sound.model.Sound.SoundData;

public interface SoundDataService {

	public void save(String objectId, Integer duration, float[][] waveData);
	
	public SoundData load(String objectId);
	
	public void delete(String objectId);
}

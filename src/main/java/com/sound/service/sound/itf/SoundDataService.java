package com.sound.service.sound.itf;

import java.util.List;

import com.sound.model.Sound.SoundData;
import com.sound.model.User;

public interface SoundDataService {

	public SoundData save(SoundData soundData);
	
	public SoundData load(String objectId);
	
	public void delete(String objectId);
	
	public List<SoundData> loadDataByOwner(User owner);
}

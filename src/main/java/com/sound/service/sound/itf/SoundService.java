package com.sound.service.sound.itf;

import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.file.LocalSoundFile;

public interface SoundService {

	public void saveProfile(String objectId, String soundAlias, String description, String ownerAlias, String status, String posterId) throws SoundException;

	public LocalSoundFile uniform(LocalSoundFile sound) throws SoundException;
	
	public void addToSet(String soundId, String setId);

	public void delete(String soundAlias);

	public Sound load(String soundId);
}

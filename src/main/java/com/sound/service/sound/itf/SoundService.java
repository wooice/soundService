package com.sound.service.sound.itf;

import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.file.LocalSoundFile;
import com.sound.model.file.RemoteFile;

public interface SoundService {

	/**
	 * 	1. convert sound data to uniform type(mp3?)
	 * 	2. extract wave form data.
	 * 	3. save sound metadata, wave form data into mongo db.
	 * 	4. save ziped origin sound data into gridfs as back up.
	 */
	public void save(LocalSoundFile sound, RemoteFile remoteFile);

	public LocalSoundFile uniform(LocalSoundFile sound) throws SoundException;
	
	public void addToSet(String soundId, String setId);

	public void delete(String soundId);

	public Sound load(String soundId);
}

package com.sound.service.sound.itf;

import java.io.File;
import java.util.List;

import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.Sound.QueueNode;
import com.sound.model.Sound.SoundProfile;
import com.sound.model.SoundActivity.SoundRecord;
import com.sound.model.file.SoundLocal;

public interface SoundService {
  
    public SoundProfile updateProfile(SoundProfile soundProfile) throws SoundException ;
  
	public SoundProfile saveProfile(SoundProfile soundProfile, String ownerAlias) throws SoundException;

	public void addToSet(String soundId, String setId);

	public void delete(String soundAlias);

	public Sound load(String userAlias, String soundId);
	
	public Sound loadByRemoteId(String remoteId);
	
	public List<SoundRecord> getSoundsByUser(String userAlias, Integer pageNum, Integer soundsPerPage) throws SoundException;
	
	public List<SoundRecord> getObservingSounds(String userAlias, Integer pageNum, Integer soundsPerPage) throws SoundException;
	
	public SoundLocal processSound(String userAlias, File originSoundFile, String fileName)  throws SoundException ;
	
	public void saveData(SoundLocal soundFile, String ownerAlias);
	
	public void checkUploadCap(String userAlias, File soundFile) throws SoundException;
	
	public void enqueue(QueueNode node);
	
	public List<QueueNode> listQueue();
	
	public void dequeue(QueueNode node);
	
	public Sound getUnfinishedUpload(String userAlias);
}

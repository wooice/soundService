package com.sound.service.sound.itf;

import java.io.File;
import java.util.Date;
import java.util.List;

import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.SoundLocal;
import com.sound.model.Sound.QueueNode;
import com.sound.model.Sound.SoundProfile;
import com.sound.model.User;
import com.sound.processor.exception.AudioProcessException;

public interface SoundService {

  public Sound updateProfile(String id, SoundProfile soundProfile) throws SoundException;

  public Sound saveProfile(SoundProfile soundProfile, User user) throws SoundException;

  public void addToSet(String soundId, String setId);

  public void delete(String soundAlias);

  public Sound load(User user, String soundId);

  public Sound loadByRemoteId(String remoteId);

  public List<Sound> loadByKeyWords(User user, String keyWords, Integer pageNum,
      Integer soundsPerPage);

  public List<Sound> getSoundsByUser(User user, User curUser, Integer pageNum, Integer soundsPerPage)
      throws SoundException;

  public List<Sound> getObservingSounds(User user, Integer pageNum, Integer soundsPerPage)
      throws SoundException;

  public SoundLocal processSoundV2(User user, File soundFile, String fileName)
      throws SoundException, AudioProcessException;

  public void saveData(SoundLocal soundFile, User owner);

  public void enqueue(QueueNode node);

  public List<QueueNode> listQueue();

  public void dequeue(QueueNode node);

  public Sound getUnfinishedUpload(User user);

  public boolean isOwner(User user, String soundAlias);

  Sound loadById(String soundId);

  public long hasNewSounds(User currentUser, Date time);
  
  public long hasNewCreated(User user, Date time);
}

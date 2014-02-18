package com.sound.service.sound.itf;

import java.util.Date;
import java.util.List;

import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.Sound.SoundProfile;
import com.sound.model.Tag;
import com.sound.model.User;

public interface SoundService {

  public Sound updateProfile(String id, SoundProfile soundProfile) throws SoundException;

  public Sound saveProfile(SoundProfile soundProfile, User user) throws SoundException;

  public void addToSet(String soundId, String setId);

  public void delete(String soundAlias);
  
  public void deleteByRemoteId(String remoteId);

  public Sound load(User user, String soundId);
  
  public List<Sound> loadByKeyWords(User user, String keyWords, Integer pageNum,
      Integer soundsPerPage);
  
  public List<Sound> loadByTags(User user, List<Tag> tags, Integer pageNum,
      Integer soundsPerPage);
  
  public List<Sound> loadUserHistory(User user, Integer pageNum, Integer soundsPerPage);

  public List<Sound> getSoundsByUser(User user, User curUser, Integer pageNum, Integer soundsPerPage)
      throws SoundException;

  public List<Sound> getObservingSounds(User user, Integer pageNum, Integer soundsPerPage)
      throws SoundException;

  public Sound getUnfinishedUpload(User user);

  public boolean isOwner(User user, String soundAlias);

  Sound loadById(String soundId);

  public long hasNewSounds(User currentUser, Date time);
  
  public long hasNewCreated(User user, Date time);
  
  public void promoteSound(Sound sound);
  
  public void demoteSound(Sound sound);
  
  public void promoteUser(User user);
  
}

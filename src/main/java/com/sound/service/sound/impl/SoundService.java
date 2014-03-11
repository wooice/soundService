package com.sound.service.sound.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sound.constant.Constant;
import com.sound.dao.QueueNodeDAO;
import com.sound.dao.SoundDAO;
import com.sound.dao.TagDAO;
import com.sound.dao.UserConnectDAO;
import com.sound.dao.UserDAO;
import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.Sound.QueueNode;
import com.sound.model.Sound.SoundProfile;
import com.sound.model.Sound.SoundProfile.SoundPoster;
import com.sound.model.Sound.SoundRight;
import com.sound.model.Sound.SoundSocial;
import com.sound.model.Sound.UserPrefer;
import com.sound.model.SoundActivity.SoundLike;
import com.sound.model.SoundActivity.SoundRecord;
import com.sound.model.Tag;
import com.sound.model.User;
import com.sound.model.User.UserRole;
import com.sound.model.UserActivity.UserConnect;
import com.sound.model.enums.SoundState;
import com.sound.model.enums.SoundType;
import com.sound.processor.factory.ProcessorFactory;
import com.sound.service.sound.itf.PlayListService;
import com.sound.service.storage.itf.RemoteStorageService;

@Service
@Scope("singleton")
public class SoundService implements com.sound.service.sound.itf.SoundService {
  @Autowired
  SoundDAO soundDAO;

  @Autowired
  UserDAO userDAO;

  @Autowired
  UserConnectDAO userConnectDAO;

  @Autowired
  TagDAO tagDAO;

  @Autowired
  QueueNodeDAO queueNodeDAO;
  
  @Autowired
  UtilService util;

  @Autowired
  ProcessorFactory processFactory;

  @Autowired
  RemoteStorageService remoteStorageService;
  
  @Autowired
  PlayListService playListService;

  @Override
  public void addToSet(String soundId, String setId) {}

  @Override
  public void delete(String id) {
    Sound sound = soundDAO.findOne("_id", new ObjectId(id));

    if (null != sound) {
      userDAO.decrease("_id", sound.getProfile().getOwner().getId(), "userSocial.sounds");

      userDAO.updateProperty("_id", sound.getProfile().getOwner().getId(),
          "userSocial.soundDuration", (sound.getProfile().getOwner().getUserSocial()
              .getSoundDuration() - sound.getProfile().getDuration()));
      
      playListService.removePlayRecord(null, sound);
      
      queueNodeDAO.updateProperty("fileName", sound.getProfile().getRemoteId(), "status", "deleted");
      remoteStorageService.deleteFile("sound", sound.getProfile().getRemoteId());
      remoteStorageService.deleteFile("image", sound.getProfile().getRemoteId());
      remoteStorageService.deleteFile("wave", sound.getProfile().getRemoteId() + ".png");

      soundDAO.delete(sound);
    }
  }

  @Override
  public void deleteByRemoteId(String remoteId) {
    remoteStorageService.deleteFile("sound", remoteId);
    remoteStorageService.deleteFile("image", remoteId);
    remoteStorageService.deleteFile("wave", remoteId + ".png");
    queueNodeDAO.updateProperty("fileName", remoteId, "status", "deleted");
    
    Sound sound = soundDAO.findOne("profile.remoteId", remoteId);

    if (null != sound) {
      userDAO.updateProperty("_id", sound.getProfile().getOwner().getId(),
          "userSocial.soundDuration", (sound.getProfile().getOwner().getUserSocial()
              .getSoundDuration() - sound.getProfile().getDuration()));

      userDAO.decrease("_id", sound.getProfile().getOwner().getId(), "userSocial.sounds");
      playListService.removePlayRecord(null, sound);
    }

    soundDAO.deleteByProperty("profile.remoteId", remoteId);
  }

  @Override
  public Sound load(User user, String soundAlias) {
    Sound sound = soundDAO.findOne("profile.alias", soundAlias);
    if (null == sound) {
      return null;
    }

    buildSoundSocial(sound);

    return sound;
  }

  @Override
  public List<Sound> loadByKeyWords(User user, String keyWord, Integer pageNum,
      Integer soundsPerPage) {

    List<User> users = userDAO.findByPattern("profile.alias", keyWord, true);
    List<Sound> sounds =
        soundDAO.findByKeyWord(keyWord, users, (pageNum - 1) * soundsPerPage, soundsPerPage);

    for (Sound sound : sounds) {
      buildSoundSocial(sound);
    }

    return sounds;
  }

  @Override
  public List<Sound> loadByTags(User user, List<Tag> tags, Integer pageNum, Integer soundsPerPage) {

    List<Sound> sounds =
        soundDAO.findByTag(user, tags, (pageNum - 1) * soundsPerPage, soundsPerPage);

    for (Sound sound : sounds) {
      buildSoundSocial(sound);
    }

    return sounds;
  }

  @Override
  public Sound saveProfile(SoundProfile soundProfile, User owner) throws SoundException {
    if (null == soundProfile.getName()) {
      throw new SoundException("Sound name can't be null.");
    }

    if (null == soundProfile.getRemoteId()) {
      throw new SoundException("Remote id can't be null.");
    }

    Sound sound = soundDAO.findOne("profile.remoteId", soundProfile.getRemoteId());

    if (null == sound) {
      sound = new Sound();
      sound.setProfile(new SoundProfile());
    }

    SoundProfile profile = sound.getProfile();
    profile.setName(soundProfile.getName());
    profile.setStatus(soundProfile.getStatus());

    if (!util.contianInvalidWords(soundProfile.getDescription()))
    {
      profile.setDescription(soundProfile.getDescription());
    }
    profile.setCreatedTime(new Date());
    profile.setDownloadable(soundProfile.isDownloadable());
    profile.setModifiedTime(new Date());
    profile.setPriority(Constant.SOUND_NORMAL);
    profile.setPriorityUpdatedDate(soundProfile.getCreatedTime());
    profile.setAlias(calculateAlias(soundProfile.getName()));
    profile.setType(SoundType.SOUND.getTypeName());
    profile.setOwner(owner);
    profile.setRemoteId(soundProfile.getRemoteId());
    profile.setCommentMode(soundProfile.getCommentMode());
    profile.setRecordType(soundProfile.getRecordType());
    profile.setUploadType(soundProfile.getUploadType());

    SoundPoster poster = new SoundPoster();
    if (null != soundProfile.getPoster()) {
      poster.setUrl(remoteStorageService.getDownloadURL(sound.getProfile().getRemoteId(), "image",
          "imageView/2/w/200/h/200/format/png"));
    } else {
      poster.setUrl("img/voice.jpg");
    }
    profile.setPoster(poster);

    if (null == soundProfile.getSoundRight()) {
      SoundRight right = new SoundRight();
      profile.setSoundRight(right);
    } else {
      profile.setSoundRight(soundProfile.getSoundRight());
    }

    SoundSocial soundSocial = new SoundSocial();
    sound.setSoundSocial(soundSocial);

    SoundRecord soundCreate = new SoundRecord();
    soundCreate.setOwner(owner);
    soundCreate.setCreatedTime(new Date());
    soundCreate.setType(Constant.SOUND_RECORD_CREATE);
    sound.addRecord(soundCreate);

    // Add one sound count to user social
    userDAO.increase("_id", owner.getId(), "userSocial.sounds");
    soundDAO.save(sound);

    return sound;
  }

  @Override
  public List<Sound> getSoundsByUser(User owner, User curUser, Integer pageNum,
      Integer soundsPerPage) {
    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("owner", owner);
    List<Sound> sounds =
        soundDAO.getUserSound(owner, curUser, (pageNum - 1) * soundsPerPage, soundsPerPage);

    for (Sound sound : sounds) {
      buildSoundSocial(sound);
    }

    return sounds;
  }

  @Override
  public List<Sound> getObservingSounds(User user, Integer pageNum, Integer soundsPerPage)
      throws SoundException {
    List<UserConnect> connections = userConnectDAO.find("fromUser", user);
    List<User> users = new ArrayList<User>();

    for (UserConnect connect : connections) {
      users.add(connect.getToUser());
    }
    List<Sound> sounds =
        soundDAO.getUsersSound(users, user, (pageNum - 1) * soundsPerPage, soundsPerPage);

    for (Sound oneSound : sounds) {
      buildSoundSocial(oneSound);
    }

    return sounds;
  }

  @Override
  public Sound updateProfile(String id, SoundProfile soundProfile) throws SoundException {
    if (null == id) {
      throw new SoundException("ID_NULL");
    }

    if (null != soundProfile.getName()) {
      soundDAO.updateProperty("_id", new ObjectId(id), "profile.name", soundProfile.getName());
      String newAlias = calculateAlias(soundProfile.getName());

      soundDAO.updateProperty("_id", new ObjectId(id), "profile.alias", newAlias);
      soundProfile.setAlias(newAlias);
    }
    if (null != soundProfile.getDescription() && !util.contianInvalidWords(soundProfile.getDescription())) {
      soundDAO.updateProperty("_id", new ObjectId(id), "profile.description",
          soundProfile.getDescription());
    }

    if (null != soundProfile.getStatus()) {
      soundDAO.updateProperty("_id", new ObjectId(id), "profile.status",
          SoundState.getStateId(soundProfile.getStatus()));
    }

    if (null != soundProfile.getPoster()) {
      soundProfile.getPoster().setUrl(
          remoteStorageService.getDownloadURL(soundProfile.getRemoteId(), "image",
              "imageView/2/w/200/h/200/format/png"));
      soundDAO.updateProperty("_id", new ObjectId(id), "profile.poster", soundProfile.getPoster());
    } 

    if (null != soundProfile.getCommentMode()) {
      soundDAO.updateProperty("_id", new ObjectId(id), "profile.commentMode",
          soundProfile.getCommentMode());
    }

    return soundDAO.findOne("_id", new ObjectId(id));
  }

  @Override
  public Sound getUnfinishedUpload(User user) {
    List<Sound> unfinishedSounds = new ArrayList<Sound>();

    // For sounds profile saved or processed.
    List<Sound> sounds = soundDAO.find("profile.owner", user);
    for (Sound sound : sounds) {
      QueueNode node = queueNodeDAO.findOne("fileName", sound.getProfile().getRemoteId());
      if (null != sound.getProfile().getAlias()) {
        // profile saved, but data not uploaded. When sound uploaded, a queue node will be generated
        // with status 'live'.
        if (null == node) {
          unfinishedSounds.add(sound);
        }
      } else {
        // profile not saved
        unfinishedSounds.add(sound);
      }
    }

    Map<String, Object> cretiaries = new HashMap<String, Object>();
    cretiaries.put("owner", user);
    cretiaries.put("status", "live");
    // For sounds file uploaded(without processing), but profile not saved
    List<QueueNode> nodes = queueNodeDAO.find(cretiaries);
    for (QueueNode node : nodes) {
      Sound sound = soundDAO.findOne("profile.remoteId", node.getFileName());
      if (null == sound) {
        sound = new Sound();
        SoundProfile profile = new SoundProfile();
        profile.setRemoteId(node.getFileName());
        profile.setName(node.getOriginFileName());
        profile.setUploadType(node.getType());
        sound.setProfile(profile);
        unfinishedSounds.add(sound);
      }
    }

    if (unfinishedSounds.size() > 0) {
      Sound soundToUpload = unfinishedSounds.get(0);
      return soundToUpload;
    } else {
      return null;
    }
  }

  @Override
  public Sound loadById(String soundId) {
    return soundDAO.findOne("_id", new ObjectId(soundId));
  }

  @Override
  public boolean isOwner(User user, String soundId) {
    Sound sound = soundDAO.findOne("_id", new ObjectId(soundId));

    if (null != sound) {
      return sound.getProfile().getOwner().equals(user);
    } else {
      return true;
    }
  }

  @Override
  public long hasNewSounds(User user, Date time) {
    List<UserConnect> connections = userConnectDAO.find("fromUser", user);
    List<User> users = new ArrayList<User>();

    for (UserConnect connect : connections) {
      users.add(connect.getToUser());
    }

    return soundDAO.getUsersSoundCount(users, time);
  }

  @Override
  public long hasNewCreated(User user, Date time) {
    return soundDAO.getUserCreatedCount(user, time);
  }

  @Override
  public void promoteSound(Sound sound) {
    sound.getProfile().setPriority(Constant.SOUND_HIGHLIGHT);
    sound.getProfile().setPriorityUpdatedDate(new Date());
    soundDAO.save(sound);
  }

  @Override
  public void demoteSound(Sound sound) {
    sound.getProfile().setPriority(Constant.SOUND_NORMAL);
    sound.getProfile().setPriorityUpdatedDate(sound.getProfile().getCreatedTime());
    soundDAO.save(sound);
  }

  @Override
  public void promoteUser(User user) {
    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("profile.owner", user);
    cratiaries.put("profile.recordType", "original");
    List<Sound> sounds = soundDAO.find(cratiaries);

    if (sounds.size() >= 5 && user.getUserRoles().contains(Constant.USER_ROLE)) {
      List<UserRole> roles = new ArrayList<UserRole>();
      roles.add(Constant.PRO_ROLE_OBJ);
      user.setUserRoles(roles);
      userDAO.save(user);
    }

    if (sounds.size() >= 9
        && (user.getUserRoles().contains(Constant.USER_ROLE) || user.getUserRoles().contains(
            Constant.PRO_ROLE_OBJ))) {
      List<UserRole> roles = new ArrayList<UserRole>();
      roles.add(Constant.SPRO_ROLE_OBJ);
      user.setUserRoles(roles);
      userDAO.save(user);
    }
  }

  @Override
  public List<Sound> loadUserHistory(User user, Integer pageNum, Integer soundsPerPage) {
    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("plays.owner", user);
    List<Sound> sounds = soundDAO.find(cratiaries);
    for (Sound sound : sounds) {
      buildSoundSocial(sound);
    }

    return sounds;
  }

  @Override
  public void buildSoundSocial(Sound sound) {
    SoundSocial soundSoical = new SoundSocial();
    soundSoical.setCommentsCount(sound.getComments().size());
    soundSoical.setLikesCount(sound.getLikes().size());
    soundSoical.setPlayedCount(sound.getPlays().size());
    soundSoical.setReportsCount(sound.getRecords().size() - 1);
    soundSoical.setVisitsCount(sound.getVisits().size());
    sound.setSoundSocial(soundSoical);
  }

  @Override
  public UserPrefer getUserPreferOfSound(Sound sound, User user) {
    if (null == sound || null == user) {
      return null;
    }

    UserPrefer userPrefer = new UserPrefer();
    userPrefer.setLike(0);
    userPrefer.setRepost(0);

    for (SoundLike like : sound.getLikes()) {
      if (like.getOwner().equals(user)) {
        userPrefer.setLike(1);
        break;
      }
    }

    for (SoundRecord repost : sound.getRecords()) {
      if (repost.getType().equals(Constant.SOUND_RECORD_REPOST) && repost.getOwner().equals(user)) {
        userPrefer.setRepost(1);
        break;
      }
    }

    return userPrefer;
  }

private String calculateAlias(String soundName) {
    long sameNames = soundDAO.count("profile.name", soundName);

    int defaultAliasLength = 7;
    String alias = soundName.replaceAll("\\s+|\\pP", "-");

    if (sameNames > 0) {
      if (alias.length() > (defaultAliasLength + sameNames)) {
        alias = alias.substring(0, (int) (defaultAliasLength + sameNames));
      } else {
        alias += ("-" + sameNames);
      }
    } else {
      if (alias.length() > defaultAliasLength) {
        alias = alias.substring(0, defaultAliasLength);
      }
    }

    Random random = new Random();
    while (null != soundDAO.findOne("profile.alias", alias)) {
      alias += random.nextInt(10);
    }

    return alias;
  }

}

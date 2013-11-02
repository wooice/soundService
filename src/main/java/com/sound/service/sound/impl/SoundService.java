package com.sound.service.sound.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
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
import com.sound.model.Sound.SoundData;
import com.sound.model.Sound.SoundFormat;
import com.sound.model.Sound.SoundProfile;
import com.sound.model.Sound.SoundProfile.SoundPoster;
import com.sound.model.Sound.SoundSocial;
import com.sound.model.Sound.UserPrefer;
import com.sound.model.SoundActivity.SoundLike;
import com.sound.model.SoundActivity.SoundRecord;
import com.sound.model.SoundLocal;
import com.sound.model.Tag;
import com.sound.model.User;
import com.sound.model.User.UserRole;
import com.sound.model.UserActivity.UserConnect;
import com.sound.model.enums.SoundState;
import com.sound.model.enums.SoundType;
import com.sound.processor.exception.AudioProcessException;
import com.sound.processor.factory.ProcessorFactory;
import com.sound.processor.itf.Extractor;
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
  SoundDataService soundDataService;

  @Autowired
  QueueNodeDAO queueNodeDAO;

  @Autowired
  ProcessorFactory processFactory;

  @Autowired
  RemoteStorageService remoteStorageService;

  @Override
  public void addToSet(String soundId, String setId) {}

  @Override
  public void delete(String id) {
    Sound sound = soundDAO.findOne("_id", new ObjectId(id));

    if (null != sound) {
      userDAO.decrease("_id", sound.getProfile().getOwner().getId(), "userSocial.sounds");
      userDAO.updateProperty("_id", sound.getProfile().getOwner().getId(),
          "userSocial.soundDuration", (sound.getProfile().getOwner().getUserSocial()
              .getSoundDuration() + sound.getSoundData().getDuration()));

      if (null != sound.getSoundData() && null != sound.getSoundData().getObjectId()) {
        soundDataService.delete(sound.getSoundData().getObjectId());
      }

      queueNodeDAO.deleteByProperty("fileName", sound.getProfile().getRemoteId());

      remoteStorageService.deleteFile("sound", sound.getProfile().getRemoteId());
      remoteStorageService.deleteFile("image", sound.getProfile().getRemoteId());

      soundDAO.delete(sound);
    }
  }

  @Override
  public Sound load(User user, String soundAlias) {
    Sound sound = soundDAO.findOne("profile.alias", soundAlias);
    if (null == sound) {
      return null;
    }

    buildSoundSocial(sound);
    generateSoundPoster(sound);
    sound.setUserPrefer(getUserPreferOfSound(sound, user));

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
      generateSoundPoster(sound);
      sound.setUserPrefer(getUserPreferOfSound(sound, user));
    }

    return sounds;
  }

  @Override
  public List<Sound> loadByTags(User user, List<Tag> tags, Integer pageNum, Integer soundsPerPage) {

    List<Sound> sounds =
        soundDAO.findByTag(user, tags, (pageNum - 1) * soundsPerPage, soundsPerPage);

    for (Sound sound : sounds) {
      buildSoundSocial(sound);
      generateSoundPoster(sound);
      sound.setUserPrefer(getUserPreferOfSound(sound, user));
    }

    return sounds;
  }

  @Override
  public void saveData(SoundLocal soundLocal, User owner) throws SoundException {
    SoundData soundData = soundDataService.load(soundLocal.getFileName());

    if (null == soundData) {
      soundData = new SoundData();
      soundData.setObjectId(soundLocal.getFileName());
      soundData.setOriginName(soundLocal.getOriginName());
      soundData.setDuration(soundLocal.getSoundFormat().getDuration());
      soundData.setOwner(owner);
      soundData.setWave(soundLocal.getWave().getWaveData());
      soundData.setSoundFormat(soundLocal.getSoundFormat());
      soundDataService.save(soundData);
    } else {
      soundData.setDuration(soundLocal.getSoundFormat().getDuration());
      soundData.setOriginName(soundLocal.getOriginName());
      soundData.setWave(soundLocal.getWave().getWaveData());
      soundData.setSoundFormat(soundLocal.getSoundFormat());
      soundDataService.update(soundData);
    }

    Sound sound = soundDAO.findOne("profile.remoteId", soundLocal.getFileName());

    if (null != sound) {
      sound.setSoundData(soundData);
      sound.getProfile().setDuration(soundLocal.getSoundFormat().getDuration());

      SoundRecord soundCreate = new SoundRecord();
      soundCreate.setOwner(owner);
      soundCreate.setCreatedTime(new Date());
      soundCreate.setType(Constant.SOUND_RECORD_CREATE);
      sound.addRecord(soundCreate);

      soundDAO.save(sound);
      userDAO.increase("_id", owner.getId(), "userSocial.sounds");
    }

    userDAO.updateProperty("_id", owner.getId(), "userSocial.soundDuration", (owner.getUserSocial()
        .getSoundDuration() + soundLocal.getSoundFormat().getDuration()));
  }

  @Override
  public Sound saveProfile(SoundProfile soundProfile, User owner) throws SoundException {
    if (null == soundProfile.getName()) {
      throw new SoundException("Sound name can't be null.");
    }

    if (null == soundProfile.getRemoteId()) {
      throw new SoundException("Remote id can't be null.");
    }

    Sound sound = new Sound();

    soundProfile.setCreatedTime(new Date());
    soundProfile.setDownloadable(false);
    soundProfile.setModifiedTime(new Date());
    soundProfile.setPriority(Constant.SOUND_NORMAL);
    soundProfile.setPriorityUpdatedDate(soundProfile.getCreatedTime());

    String alias = calculateAlias(soundProfile.getName());

    soundProfile.setAlias(alias);
    soundProfile.setType(SoundType.SOUND.getTypeName());

    soundProfile.setOwner(owner);

    if (null == soundProfile.getPoster()) {
      SoundPoster soundPoster = new SoundPoster();
      soundProfile.setPoster(soundPoster);
    }

    SoundSocial soundSocial = new SoundSocial();
    sound.setSoundSocial(soundSocial);

    String remoteId = soundProfile.getRemoteId();
    SoundData soundData = soundDataService.load(remoteId);
    if (null != soundData) {
      sound.setSoundData(soundData);

      SoundRecord soundCreate = new SoundRecord();
      soundCreate.setOwner(owner);
      soundCreate.setCreatedTime(new Date());
      soundCreate.setType(Constant.SOUND_RECORD_CREATE);
      sound.addRecord(soundCreate);

      soundProfile.setDuration(soundData.getDuration());

      // Add one sound count to user social
      userDAO.increase("_id", owner.getId(), "userSocial.sounds");
    }

    sound.setProfile(soundProfile);
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
      generateSoundPoster(sound);
      sound.setUserPrefer(getUserPreferOfSound(sound, owner));
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
      generateSoundPoster(oneSound);
      oneSound.setUserPrefer(getUserPreferOfSound(oneSound, user));
    }

    return sounds;
  }

  private boolean checkUserRight(User user, SoundFormat soundFormat) {
    if (null != soundFormat.getAlbum_artist()
        && soundFormat.getAlbum_artist().equals(user.getProfile().getAlias())) {
      return true;
    }

    if (null != soundFormat.getAlbum_artist()
        && soundFormat.getAlbum_artist().equals(
            user.getProfile().getLastName() + user.getProfile().getFirstName())) {
      return true;
    }

    if (null != soundFormat.getArtist()
        && soundFormat.getArtist().equals(user.getProfile().getAlias())) {
      return true;
    }

    if (null != soundFormat.getArtist()
        && soundFormat.getArtist().equals(
            user.getProfile().getLastName() + user.getProfile().getFirstName())) {
      return true;
    }

    if (null != soundFormat.getComposer()
        && soundFormat.getComposer().equals(user.getProfile().getAlias())) {
      return true;
    }

    if (null != soundFormat.getComposer()
        && soundFormat.getComposer().equals(
            user.getProfile().getLastName() + user.getProfile().getFirstName())) {
      return true;
    }

    if (null != soundFormat.getPerformer()
        && soundFormat.getPerformer().equals(user.getProfile().getAlias())) {
      return true;
    }

    if (null != soundFormat.getPerformer()
        && soundFormat.getPerformer().equals(
            user.getProfile().getLastName() + user.getProfile().getFirstName())) {
      return true;
    }

    return false;
  }

  @Override
  public SoundLocal processSound(User user, String soundUrl, String fileName)
      throws SoundException, AudioProcessException {
    Extractor extractor = processFactory.getExtractor("wav");
    String soundInfoString = this.getSoundInfo(fileName);

    if (null == soundInfoString || !soundInfoString.contains("streams")) {
      throw new SoundException("EMPTY_STREAM");
    }

    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

      JsonNode rootNode = mapper.readTree(soundInfoString);
      JsonNode formatNode = rootNode.path("format").path("tags");
      SoundFormat soundFormat = mapper.readValue(formatNode, SoundFormat.class);
      soundFormat.setDuration((float) rootNode.findValue("duration").asDouble());

      if (!checkUserRight(user, soundFormat)) {
        throw new SoundException("NO_RIGHT");
      }

      if (user.getUserRoles().contains(Constant.USER_ROLE)
          || user.getUserRoles().contains(Constant.PRO_ROLE_OBJ)) {
        if ((user.getUserSocial().getSoundDuration() + soundFormat.getDuration()) > user
            .getUserRoles().get(0).getAllowedDuration() * 60) {
          throw new SoundException("TOTAL_LIMIT_ERROR");
        }
      }

      // User can't upload sounds more than Constant.WEEKLY_ALLOWED_DURATION limitaion in recent one
      // week
      Map<String, Object> cratiaries = new HashMap<String, Object>();
      cratiaries.put("profile.owner", user);
      Calendar c = Calendar.getInstance();
      c.add(Calendar.DATE, -7);
      Date startDate = c.getTime();
      List<Sound> sounds = soundDAO.getSoundsByCreatedTime(cratiaries, startDate);
      long totalDuration = 0;
      for (Sound sound : sounds) {
        totalDuration += sound.getProfile().getDuration();
      }
      if (totalDuration > Constant.WEEKLY_ALLOWED_DURATION) {
        throw new SoundException("WEEKLY_LIMIT_ERROR");
      }

      SoundLocal soundLocal = new SoundLocal();
      soundLocal.setFileName(fileName);
      soundLocal.setWave(extractor.extractWaveByTotal(new URL(soundUrl), null));
      soundLocal.setSoundFormat(soundFormat);

      return soundLocal;
    } catch (JsonProcessingException e) {
      throw new SoundException("NO_FORMATINFO");
    } catch (IOException e) {
      throw new SoundException("NO_FORMATINFO");
    }
  }

  @Override
  public Sound updateProfile(String id, SoundProfile soundProfile) throws SoundException {
    if (null == id) {
      throw new SoundException("Id is a must.");
    }

    if (null != soundProfile.getName()) {
      soundDAO.updateProperty("_id", new ObjectId(id), "profile.name", soundProfile.getName());
      String newAlias = calculateAlias(soundProfile.getName());

      soundDAO.updateProperty("_id", new ObjectId(id), "profile.alias", newAlias);
      soundProfile.setAlias(newAlias);
    }

    if (null != soundProfile.getDescription()) {
      soundDAO.updateProperty("_id", new ObjectId(id), "profile.description",
          soundProfile.getDescription());
    }

    if (null != soundProfile.getStatus()) {
      soundDAO.updateProperty("_id", new ObjectId(id), "profile.status",
          SoundState.getStateId(soundProfile.getStatus()));
    }

    if (null != soundProfile.getPoster()) {
      soundDAO.updateProperty("_id", new ObjectId(id), "profile.poster", soundProfile.getPoster());
    }

    if (null != soundProfile.getCommentMode()) {
      soundDAO.updateProperty("_id", new ObjectId(id), "profile.commentMode",
          soundProfile.getCommentMode());
    }

    return soundDAO.findOne("_id", new ObjectId(id));
  }

  @Override
  public void enqueue(QueueNode queueNode) {
    QueueNode node = queueNodeDAO.findOne("fileName", queueNode.getFileName());
    if (null == node) {
      node = new QueueNode();
    }
    node.setOwner(queueNode.getOwner());
    node.setFileName(queueNode.getFileName());
    node.setOriginFileName(queueNode.getOriginFileName());
    node.setCreatedDate(new Date());
    queueNodeDAO.save(node);
  }

  @Override
  public List<QueueNode> listQueue() {
    return queueNodeDAO.find().asList();
  }

  @Override
  public void dequeue(QueueNode node) {
    queueNodeDAO.delete(node);
  }

  @Override
  public Sound getUnfinishedUpload(User user) {
    List<Sound> unfinishedSounds = new ArrayList<Sound>();

    List<Sound> sounds = soundDAO.find("profile.owner", user);
    for (Sound sound : sounds) {
      if (null == sound.getSoundData()) {
        QueueNode node = queueNodeDAO.findOne("fileName", sound.getProfile().getRemoteId());
        if (null == node) {
          unfinishedSounds.add(sound);
        }
      }
    }

    List<SoundData> soundDatas = soundDataService.loadDataByOwner(user);
    for (SoundData soundData : soundDatas) {
      Sound ownerSound = soundDAO.findOne("soundData", soundData);
      if (null == ownerSound) {
        ownerSound = new Sound();
        ownerSound.setSoundData(soundData);
        unfinishedSounds.add(ownerSound);
      }
    }

    List<QueueNode> nodes = queueNodeDAO.find("owner", user);
    for (QueueNode node : nodes) {
      Sound sound = soundDAO.findOne("profile.remoteId", node.getFileName().split("\\.")[0]);
      if (null == sound) {
        sound = new Sound();
        SoundData soundData = new SoundData();
        soundData.setObjectId(node.getFileName());
        soundData.setOriginName(node.getOriginFileName());
        sound.setSoundData(soundData);
        unfinishedSounds.add(sound);
      }
    }

    if (unfinishedSounds.size() > 0) {
      Sound soundToUpload = unfinishedSounds.get(0);
      if (null != soundToUpload.getSoundData()) {
        soundToUpload.getSoundData().setWave(null);
      }
      return soundToUpload;
    } else {
      return null;
    }
  }

  @Override
  public Sound loadByRemoteId(String remoteId) {
    return soundDAO.findOne("profile.remoteId", remoteId);
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
    return alias;
  }

  private UserPrefer getUserPreferOfSound(Sound sound, User user) {
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


  private void buildSoundSocial(Sound sound) {
    SoundSocial soundSoical = new SoundSocial();
    soundSoical.setCommentsCount(sound.getComments().size());
    soundSoical.setLikesCount(sound.getLikes().size());
    soundSoical.setPlayedCount(sound.getPlays().size());
    soundSoical.setReportsCount(sound.getRecords().size() - 1);
    soundSoical.setVisitsCount(sound.getVisits().size());
    sound.setSoundSocial(soundSoical);

    sound.getProfile().setUrl(
        remoteStorageService.getDownloadURL(sound.getSoundData().getObjectId(), "sound",
            "avthumb/mp3"));
  }

  private void generateSoundPoster(Sound sound) {
    if (null != sound.getProfile().getPoster()
        && null != sound.getProfile().getPoster().getPosterId()) {
      sound
          .getProfile()
          .getPoster()
          .setUrl(
              remoteStorageService.getDownloadURL(sound.getProfile().getPoster().getPosterId(),
                  "image", "imageView/2/w/200/h/200/format/png"));
    } else {
      SoundPoster poster = new SoundPoster();
      poster.setUrl("img/voice.jpg");
      sound.getProfile().setPoster(poster);
    }
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
  public List<SoundData> loadData(User user, List<String> soundIds) {
    List<ObjectId> soundIdObjects = new ArrayList<ObjectId>();
    for (String soundId : soundIds) {
      soundIdObjects.add(new ObjectId(soundId));
    }
    List<Sound> sounds = soundDAO.getSoundsByIds(soundIdObjects);
    List<SoundData> soundData = new ArrayList<SoundData>();

    for (Sound sound : sounds) {
      SoundData newData = sound.getSoundData();
      newData.setSoundId(sound.getId().toString());
      newData.setCommentMode(sound.getProfile().getCommentMode());
      soundData.add(newData);
    }

    return soundData;
  }

  @Override
  public void promoreUser(User user) {
    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("profile.owner", user);
    cratiaries.put("profile.recordType", "original");
    List<Sound> sounds = soundDAO.find(cratiaries);

    if (sounds.size() >= 3 && user.getUserRoles().contains(Constant.USER_ROLE)) {
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
  public String getSoundInfo(String remoteId) {
    String infoURL = remoteStorageService.getDownloadURL(remoteId, "sound", "avinfo");
    try {
      HttpClient httpClient = new DefaultHttpClient();
      HttpGet httpget = new HttpGet(infoURL);
      HttpResponse httpresponse = httpClient.execute(httpget);
      // 获取返回数据
      HttpEntity entity = httpresponse.getEntity();
      return EntityUtils.toString(entity);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public List<Sound> loadUserHistory(User user, Integer pageNum, Integer soundsPerPage) {
    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("plays.owner", user);
    List<Sound> sounds = soundDAO.find(cratiaries);
    for (Sound sound : sounds) {
      buildSoundSocial(sound);
      generateSoundPoster(sound);
      sound.setUserPrefer(getUserPreferOfSound(sound, user));
    }
    
    return sounds;
  }
  
}

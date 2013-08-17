package com.sound.service.sound.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sound.dao.QueueNodeDAO;
import com.sound.dao.SoundDAO;
import com.sound.dao.SoundLikeDAO;
import com.sound.dao.SoundRecordDAO;
import com.sound.dao.UserConnectDAO;
import com.sound.dao.UserDAO;
import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.Sound.QueueNode;
import com.sound.model.Sound.SoundData;
import com.sound.model.Sound.SoundProfile;
import com.sound.model.Sound.SoundProfile.SoundPoster;
import com.sound.model.Sound.SoundSocial;
import com.sound.model.Sound.UserPrefer;
import com.sound.model.SoundActivity.SoundLike;
import com.sound.model.SoundActivity.SoundRecord;
import com.sound.model.User;
import com.sound.model.UserActivity.UserConnect;
import com.sound.model.enums.FileType;
import com.sound.model.enums.SoundState;
import com.sound.model.enums.SoundType;
import com.sound.model.file.SoundLocal;
import com.sound.processor.exception.AudioProcessException;
import com.sound.processor.factory.ProcessorFactory;
import com.sound.processor.itf.Converter;
import com.sound.processor.itf.Extractor;
import com.sound.processor.model.AudioInfo;
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
  SoundDataService soundDataService;

  @Autowired
  SoundRecordDAO soundRecordDAO;

  @Autowired
  SoundLikeDAO soundLikeDAO;

  @Autowired
  QueueNodeDAO queueNodeDAO;

  @Autowired
  ProcessorFactory processFactory;

  @Autowired
  RemoteStorageService remoteStorageService;

  @Override
  public void addToSet(String soundId, String setId) {}

  @Override
  public void delete(String soundAlias) {
    Sound sound = soundDAO.findOne("profile.alias", soundAlias);

    if (null != sound) {
      // Delete create sound activity.
      soundRecordDAO.deleteByProperty("sound", sound);
      soundLikeDAO.deleteByProperty("sound", sound);

      if (null != sound.getSoundData() && null != sound.getSoundData().getObjectId()) {
        soundDataService.delete(sound.getSoundData().getObjectId());
      }
      soundDAO.delete(sound);

      // TODO: decrease user's sound count and total sound time.
    }
  }

  @Override
  public Sound load(String userAlias, String soundAlias) {
    Sound sound = soundDAO.findOne("profile.alias", soundAlias);
    sound.getSoundData().setUrl(
        remoteStorageService.generateDownloadUrl(
            sound.getSoundData().getObjectId() + "." + sound.getSoundData().getExtension(),
            FileType.getFileType("sound")).toString());

    if (null != sound.getProfile().getPoster()
        && null != sound.getProfile().getPoster().getPosterId()) {
      sound
          .getProfile()
          .getPoster()
          .setUrl(
              remoteStorageService.generateDownloadUrl(
                  sound.getProfile().getPoster().getPosterId() + "."
                      + sound.getProfile().getPoster().getExtension(),
                  FileType.getFileType("image")).toString());
    }

    User currentUser = userDAO.findOne("profile.alias", userAlias);
    sound.setUserPrefer(getUserPreferOfSound(sound, currentUser));

    return sound;
  }

  @Override
  public void saveData(SoundLocal soundLocal, String ownerAlias) {
    User owner = userDAO.findOne("profile.alias", ownerAlias);
    String[] fileNames = soundLocal.getFileName().split("\\.");

    SoundData soundData = soundDataService.load(fileNames[0]);

    if (null == soundData) {
      soundData = new SoundData();
      soundData.setObjectId(fileNames[0]);
      soundData.setExtension(fileNames[1]);
      soundData.setOriginName(soundLocal.getOriginName());
      soundData.setDuration(soundLocal.getDuration());
      soundData.setOwner(owner);
      soundData.setWave(soundLocal.getWave().getWaveData());
      soundDataService.save(soundData);
    } else {
      soundData.setDuration(soundLocal.getDuration());
      soundData.setOriginName(soundLocal.getOriginName());
      soundData.setWave(soundLocal.getWave().getWaveData());
      soundDataService.update(soundData);
    }

    boolean profileSaved = false;
    Sound sound = soundDAO.findOne("profile.remoteId", fileNames[0]);
    if (null != sound) {
      profileSaved = true;
      sound.setSoundData(soundData);
      soundDAO.save(sound);
    }

    if (profileSaved) {
      SoundRecord soundRecord = new SoundRecord();
      soundRecord.setOwner(owner);
      soundRecord.addAction(SoundRecord.CREATE);
      soundRecord.setSound(sound);
      soundRecord.setCreatedTime(new Date());
      soundRecordDAO.save(soundRecord);

      // Add one sound count to user social
      userDAO.increase("profile.alias", ownerAlias, "social.sounds");
    }

    userDAO.updateProperty("profile.alias", ownerAlias, "social.soundDuration", (owner
        .getUserSocial().getSoundDuration() + soundLocal.getDuration()));
  }

  @Override
  public SoundProfile saveProfile(SoundProfile soundProfile, String ownerAlias)
      throws SoundException {
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

    String alias = calculateAlias(soundProfile.getName());

    soundProfile.setAlias(alias);
    soundProfile.setType(SoundType.SOUND.getTypeName());

    User owner = userDAO.findOne("profile.alias", ownerAlias);
    soundProfile.setOwner(owner);

    if (null == soundProfile.getPoster()) {
      SoundPoster soundPoster = new SoundPoster();
      soundProfile.setPoster(soundPoster);
    }

    sound.setProfile(soundProfile);

    SoundSocial soundSocial = new SoundSocial();
    sound.setSoundSocial(soundSocial);

    boolean dataSaved = false;
    String remoteId = soundProfile.getRemoteId();

    SoundData soundData = soundDataService.load(remoteId);
    if (null != soundData) {
      sound.setSoundData(soundData);
      dataSaved = true;
    }

    soundDAO.save(sound);

    if (dataSaved) {
      SoundRecord soundRecord = new SoundRecord();
      soundRecord.setOwner(owner);
      soundRecord.addAction(SoundRecord.CREATE);
      soundRecord.setSound(sound);
      soundRecord.setCreatedTime(new Date());
      soundRecordDAO.save(soundRecord);

      // Add one sound count to user social
      userDAO.increase("profile.alias", ownerAlias, "social.sounds");
    }

    return soundProfile;
  }

  @Override
  public List<SoundRecord> getSoundsByUser(String userAlias, Integer pageNum, Integer soundsPerPage) {
    User owner = userDAO.findOne("profile.alias", userAlias);
    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("owner", owner);
    List<SoundRecord> records =
        soundRecordDAO.findWithRange(cratiaries, (pageNum - 1) * soundsPerPage, soundsPerPage,
            "-createdTime");

    Set<SoundRecord> results = new HashSet<SoundRecord>();

    for (SoundRecord oneSound : records) {
      if (!results.contains(oneSound)) {
        if (null != oneSound.getSound().getProfile().getPoster()
            && null != oneSound.getSound().getProfile().getPoster().getPosterId()) {
          oneSound
              .getSound()
              .getProfile()
              .getPoster()
              .setUrl(
                  remoteStorageService.generateDownloadUrl(
                      oneSound.getSound().getProfile().getPoster().getPosterId() + "."
                          + oneSound.getSound().getProfile().getPoster().getExtension(),
                      FileType.getFileType("image")).toString());
        }
        oneSound.getSound().setUserPrefer(getUserPreferOfSound(oneSound.getSound(), owner));
        results.add(oneSound);
      }
    }
    records.clear();
    records.addAll(results);

    return records;
  }

  @Override
  public List<SoundRecord> getObservingSounds(String userAlias, Integer pageNum,
      Integer soundsPerPage) throws SoundException {
    User currentUser = userDAO.findOne("profile.alias", userAlias);
    List<UserConnect> connections = userConnectDAO.find("fromUser", currentUser);
    List<User> users = new ArrayList<User>();

    for (UserConnect connect : connections) {
      users.add(connect.getFromUser());
    }

    Map<String, Object> cratiaries = new HashMap<String, Object>();
    List<SoundRecord> records =
        soundRecordDAO
            .findByOwners(cratiaries, users, (pageNum - 1) * soundsPerPage, soundsPerPage);

    Set<SoundRecord> results = new HashSet<SoundRecord>();

    for (SoundRecord oneSound : records) {
      if (!results.contains(oneSound)) {
        if (null != oneSound.getSound().getProfile().getPoster()
            && null != oneSound.getSound().getProfile().getPoster().getPosterId()) {
          oneSound
              .getSound()
              .getProfile()
              .getPoster()
              .setUrl(
                  remoteStorageService.generateDownloadUrl(
                      oneSound.getSound().getProfile().getPoster().getPosterId() + "."
                          + oneSound.getSound().getProfile().getPoster().getExtension(),
                      FileType.getFileType("image")).toString());
        }
        oneSound.getSound().setUserPrefer(getUserPreferOfSound(oneSound.getSound(), currentUser));
        results.add(oneSound);
      }
    }
    records.clear();
    records.addAll(results);

    return records;
  }

  @Override
  public SoundLocal processSound(String userAlias, File originSoundFile, String fileName)
      throws SoundException {
    Converter wavConverter = processFactory.getConverter("wav");
    Converter mp3Cconverter = processFactory.getConverter("mp3");
    Extractor extractor = processFactory.getExtractor("wav");
    File mp3File = null;
    File wavFile = null;

    try {
      wavFile = wavConverter.convert(originSoundFile);

      AudioInfo soundInfo = extractor.extractInfo(wavFile);

      User user = userDAO.findOne("profile.alias", userAlias);

      if ((user.getUserSocial().getSoundDuration() + soundInfo.getDuration() / 1000) > 120 * 60) {
        throw new SoundException("User " + userAlias + " can't upload more sounds.");
      }

      if (!originSoundFile.getName().endsWith(".mp3")) {
        mp3File = mp3Cconverter.convert(originSoundFile);
      } else {
        mp3File = originSoundFile;
      }

      SoundLocal soundLocal = new SoundLocal();
      soundLocal.setDuration(soundInfo.getDuration() / 1000);
      soundLocal.setFileName(fileName);
      soundLocal.setSoundStream(new FileInputStream(mp3File));
      soundLocal.setWave(extractor.extractWaveByTotal(wavFile, null));
      soundLocal.setLength(mp3File.length());

      return soundLocal;
    } catch (IOException e) {
      e.printStackTrace();
      throw new SoundException("Failed to upload sound due to unable to open sound.");
    } catch (AudioProcessException e) {
      e.printStackTrace();
      throw new SoundException("Failed to upload sound due to unable to process sound.");
    } catch (Exception e) {
      e.printStackTrace();
      throw new SoundException("Failed to upload sound due to unable to process sound.");
    } finally {
      if (null != originSoundFile) {
        originSoundFile.delete();
      }
      if (null != wavFile) {
        wavFile.delete();
      }
      if (null != mp3File) {
        mp3File.delete();
      }
    }
  }

  @Override
  public SoundProfile updateProfile(SoundProfile soundProfile) throws SoundException {
    if (null == soundProfile.getAlias()) {
      throw new SoundException("Alias is a must.");
    }

    if (null != soundProfile.getName()) {
      soundDAO.updateProperty("profile.alias", soundProfile.getAlias(), "profile.name",
          soundProfile.getName());
      String newAlias = calculateAlias(soundProfile.getName());

      soundDAO.updateProperty("profile.alias", soundProfile.getAlias(), "profile.alias", newAlias);
      soundProfile.setAlias(newAlias);
    }

    if (null != soundProfile.getDescription()) {
      soundDAO.updateProperty("profile.alias", soundProfile.getAlias(), "profile.description",
          soundProfile.getDescription());
    }

    if (null != soundProfile.getStatus()) {
      soundDAO.updateProperty("profile.alias", soundProfile.getAlias(), "profile.status",
          SoundState.getStateId(soundProfile.getStatus()));
    }

    if (null != soundProfile.getPoster()) {
      soundDAO.updateProperty("profile.alias", soundProfile.getAlias(), "profile.poster",
          soundProfile.getPoster());
    }

    Sound sound = soundDAO.findOne("profile.alias", soundProfile.getAlias());

    return sound.getProfile();
  }

  @Override
  public void enqueue(QueueNode queueNode) {
    QueueNode node = queueNodeDAO.findOne("fileName", queueNode.getFileName());
    if (null == node) {
      node = new QueueNode();
    }
    node.setOwnerAlias(queueNode.getOwnerAlias());
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
  public void checkUploadCap(String userAlias, File soundFile) throws SoundException {
    Extractor extractor = processFactory.getExtractor("wav");
    AudioInfo soundInfo = null;
    try {
      soundInfo = extractor.extractInfo(soundFile);
    } catch (AudioProcessException e) {
      e.printStackTrace();
      throw new SoundException("Failed to parse sound File");
    }

    User user = userDAO.findOne("profile.alias", userAlias);

    if ((user.getUserSocial().getSoundDuration() + soundInfo.getDuration() / 1000) > 120 * 60) {
      throw new SoundException("User " + userAlias + " can't upload more sounds.");
    }
  }

  @Override
  public void dequeue(QueueNode node) {
    queueNodeDAO.delete(node);
  }

  @Override
  public Sound getUnfinishedUpload(String userAlias) {
    User owner = userDAO.findOne("profile.alias", userAlias);
    List<Sound> unfinishedSounds = new ArrayList<Sound>();

    List<Sound> sounds = soundDAO.find("profile.owner", owner);
    for (Sound sound : sounds) {
      if (null == sound.getSoundData()) {
        QueueNode node =
            queueNodeDAO.findOne("fileName", sound.getProfile().getRemoteId() + "."
                + sound.getProfile().getExtension());
        if (null == node) {
          unfinishedSounds.add(sound);
        }
      }
    }

    List<SoundData> soundDatas = soundDataService.loadDataByOwner(owner);
    for (SoundData soundData : soundDatas) {
      Sound ownerSound = soundDAO.findOne("soundData", soundData);
      if (null == ownerSound) {
        ownerSound = new Sound();
        ownerSound.setSoundData(soundData);
        unfinishedSounds.add(ownerSound);
      }
    }

    List<QueueNode> nodes = queueNodeDAO.find("ownerAlias", userAlias);
    for (QueueNode node : nodes) {
      Sound sound = soundDAO.findOne("profile.remoteId", node.getFileName().split("\\.")[0]);
      if (null == sound) {
        sound = new Sound();
        SoundData soundData = new SoundData();
        soundData.setObjectId(node.getFileName().split("\\.")[0]);
        soundData.setExtension(node.getFileName().split("\\.")[1]);
        soundData.setOriginName(node.getOriginFileName());
        sound.setSoundData(soundData);
        unfinishedSounds.add(sound);
      }
    }

    if (unfinishedSounds.size() > 0) {
      Sound soundToUpload = unfinishedSounds.get(0);
      soundToUpload.getSoundData().setWave(null);
      return soundToUpload;
    } else {
      return null;
    }
  }

  @Override
  public Sound loadByRemoteId(String remoteId) {
    return soundDAO.findOne("profile.remoteId", remoteId);
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

    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("sound", sound);
    cratiaries.put("owner", user);
    SoundLike liked = soundLikeDAO.findOne(cratiaries);
    userPrefer.setLike((null == liked) ? 0 : 1);

    cratiaries.clear();
    cratiaries.put("sound", sound);
    cratiaries.put("owner", user);
    SoundRecord reposted = soundRecordDAO.findOne(cratiaries);
    userPrefer.setRepost((null == reposted) ? 0 : reposted.hasAction(SoundRecord.REPOST) ? 1 : 0);

    return userPrefer;
  }

}

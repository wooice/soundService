package com.sound.service.sound.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sound.dao.SoundCommentDAO;
import com.sound.dao.SoundDAO;
import com.sound.dao.SoundLikeDAO;
import com.sound.dao.SoundPlayDAO;
import com.sound.dao.SoundRecordDAO;
import com.sound.dao.UserDAO;
import com.sound.exception.SoundException;
import com.sound.exception.UserException;
import com.sound.model.Sound;
import com.sound.model.SoundActivity.SoundComment;
import com.sound.model.SoundActivity.SoundLike;
import com.sound.model.SoundActivity.SoundPlay;
import com.sound.model.SoundActivity.SoundRecord;
import com.sound.model.Tag;
import com.sound.model.User;
import com.sound.model.enums.FileType;
import com.sound.service.storage.itf.RemoteStorageService;
import com.sound.util.SocialUtils;

@Service
@Scope("singleton")
public class SoundSocialService implements com.sound.service.sound.itf.SoundSocialService {

  @Autowired
  UserDAO userDAO;

  @Autowired
  SoundDAO soundDAO;

  @Autowired
  SoundPlayDAO soundPlayDAO;

  @Autowired
  SoundLikeDAO soundLikeDAO;

  @Autowired
  SoundRecordDAO soundRecordDAO;

  @Autowired
  SoundCommentDAO soundCommentDAO;

  @Autowired
  TagService tagService;

  @Autowired
  RemoteStorageService remoteStorageService;

  @Override
  public Map<String, String> play(String soundAlias, String userAlias) throws SoundException {
    Sound sound = soundDAO.findOne("profile.alias", soundAlias);

    if (null == sound) {
      throw new SoundException("Sound with name " + soundAlias + " not found");
    }
    User user = userDAO.findOne("profile.alias", userAlias);

    SoundPlay play = new SoundPlay();
    play.setSound(sound);
    play.setOwner(user);
    play.setCreatedTime(new Date());
    soundPlayDAO.save(play);
    soundDAO.increase("profile.alias", soundAlias, "soundSocial.playedCount");

    Map<String, String> playResult = new HashMap<String, String>();
    playResult.put("played", String.valueOf(sound.getSoundSocial().getPlayedCount() + 1));
    playResult.put(
        "url",
        remoteStorageService.generateDownloadUrl(
            sound.getSoundData().getObjectId() + "." + sound.getSoundData().getExtension(),
            FileType.getFileType("sound")).toString());

    return playResult;
  }

  @Override
  public Integer like(String soundAlias, String userAlias) throws SoundException {
    Sound sound = soundDAO.findOne("profile.alias", soundAlias);

    if (null == sound) {
      throw new SoundException("Sound with name " + soundAlias + " not found");
    }

    User user = userDAO.findOne("profile.alias", userAlias);
    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("sound", sound);
    cratiaries.put("owner", user);
    SoundLike liked = soundLikeDAO.findOne(cratiaries);

    if (null != liked) {
      throw new SoundException("The user " + userAlias + " has liked sound " + soundAlias);
    }

    SoundLike like = new SoundLike();
    like.setSound(sound);
    like.setOwner(user);
    like.setCreatedTime(new Date());
    soundLikeDAO.save(like);
    soundDAO.increase("profile.alias", soundAlias, "soundSocial.likesCount");

    return sound.getSoundSocial().getLikesCount() + 1;
  }

  @Override
  public Integer dislike(String soundAlias, String userAlias) throws SoundException {
    Sound sound = soundDAO.findOne("profile.alias", soundAlias);

    if (null == sound) {
      throw new SoundException("Sound with name " + soundAlias + " not found");
    }

    User user = userDAO.findOne("profile.alias", userAlias);

    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("sound", sound);
    cratiaries.put("owner", user);
    SoundLike liked = soundLikeDAO.findOne(cratiaries);

    if (null == liked) {
      throw new SoundException("The user " + userAlias + " hasn't liked sound " + soundAlias);
    }

    soundLikeDAO.delete(liked);
    soundDAO.decrease("profile.alias", soundAlias, "soundSocial.likesCount");

    return sound.getSoundSocial().getLikesCount() - 1;
  }

  @Override
  public Integer repost(String soundAlias, String userAlias) throws SoundException {
    Sound sound = soundDAO.findOne("profile.alias", soundAlias);

    if (null == sound) {
      throw new SoundException("Sound with name " + soundAlias + " not found");
    }

    User user = userDAO.findOne("profile.alias", userAlias);

    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("sound", sound);
    cratiaries.put("owner", user);
    SoundRecord activityRecord = soundRecordDAO.findOne(cratiaries);

    if (null == activityRecord) {
      activityRecord = new SoundRecord();
      activityRecord.setSound(sound);
      activityRecord.setOwner(user);
    }
    activityRecord.addAction(SoundRecord.REPOST);
    activityRecord.setCreatedTime(new Date());
    soundRecordDAO.save(activityRecord);

    soundDAO.increase("profile.alias", soundAlias, "soundSocial.reportsCount");
    userDAO.increase("profile.alias", userAlias, "social.reposts");

    return sound.getSoundSocial().getReportsCount() + 1;
  }

  @Override
  public Integer unrepost(String soundAlias, String userAlias) throws SoundException {
    Sound sound = soundDAO.findOne("profile.alias", soundAlias);

    if (null == sound) {
      throw new SoundException("Sound with name " + soundAlias + " not found");
    }

    User user = userDAO.findOne("profile.alias", userAlias);

    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("sound", sound);
    cratiaries.put("owner", user);
    SoundRecord reposted = soundRecordDAO.findOne(cratiaries);

    if (null == reposted) {
      throw new SoundException("The user " + userAlias + " hasn't reposted sound " + soundAlias);
    }
    reposted.removeAction(SoundRecord.REPOST);
    soundRecordDAO.save(reposted);
    soundDAO.decrease("profile.alias", soundAlias, "soundSocial.reportsCount");
    userDAO.decrease("profile.alias", userAlias, "social.reposts");

    return sound.getSoundSocial().getReportsCount() - 1;
  }

  @Override
  public Integer comment(String soundAlias, String userAlias, String comment, Float pointAt)
      throws SoundException, UserException {
    SoundComment soundComment = new SoundComment();

    User user = userDAO.findOne("profile.alias", userAlias);
    if (null == user) {
      throw new UserException("User " + userAlias + " not found.");
    }
    soundComment.setOwner(user);

    Sound sound = soundDAO.findOne("profile.alias", soundAlias);
    if (null == sound) {
      throw new SoundException("Sound " + soundAlias + " not found.");
    }
    soundComment.setSound(sound);
    soundComment.setCreatedTime(new Date());
    soundComment.setComment(comment);
    soundComment.setPointAt(pointAt);

    soundCommentDAO.save(soundComment);
    soundDAO.increase("profile.alias", soundAlias, "soundSocial.commentsCount");

    return sound.getSoundSocial().getCommentsCount() + 1;
  }

  @Override
  public Integer uncomment(String commentId) throws SoundException {
    SoundComment soundComment = soundCommentDAO.findOne("id", new ObjectId(commentId));

    if (null == soundComment) {
      throw new SoundException("Sound comment doesn't exist");
    }

    int commentsCount = soundComment.getSound().getSoundSocial().getCommentsCount() - 1;

    String soundAlias = soundComment.getSound().getProfile().getName();
    soundCommentDAO.delete(soundComment);
    soundDAO.decrease("profile.alias", soundAlias, "soundSocial.commentsCount");

    return commentsCount;
  }

  @Override
  public List<SoundComment> getComments(String soundAlias, Integer pageNum, Integer commentsPerPage)
      throws SoundException {
    Sound sound = soundDAO.findOne("profile.alias", soundAlias);

    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("sound", sound);
    List<SoundComment> comments =
        soundCommentDAO.findWithRange(cratiaries, (pageNum - 1) * commentsPerPage, commentsPerPage,
            "-createdTime");

    for (SoundComment comment : comments) {
      comment.getSound().setSoundData(null);
      comment.getSound().setTags(null);
      comment.getSound().setSoundSocial(null);

      if (comment.getOwner().getProfile().hasAvatar()) {
        comment
            .getOwner()
            .getProfile()
            .setAvatorUrl(
                remoteStorageService.generateDownloadUrl(
                    comment.getOwner().getProfile().getAlias(), FileType.getFileType("image"))
                    .toString());
      } else {
        comment.getOwner().getProfile().setAvatorUrl(null);
      }
    }

    return comments;
  }

  public List<Sound> recommandSoundsByTags(List<String> tagLabels, Integer pageNum, Integer pageSize)
      throws SoundException {
    Set<Tag> tags = new HashSet<Tag>();

    for (String label : tagLabels) {
      tags.add(tagService.getOrCreate(label, null, null));
    }

    List<Sound> byTags = recommandSoundsByTags(tags);

    List<Sound> toReturn = SocialUtils.sliceList(byTags, pageNum, pageSize);

    if (toReturn.size() < pageNum) {
      toReturn.addAll(recommandRandomSounds(pageNum - toReturn.size()));
    }

    return toReturn;
  }

  private List<Sound> recommandSoundsByTags(Set<Tag> tags) throws SoundException {

    Map<Tag, List<Sound>> tagSoundMap = new HashMap<Tag, List<Sound>>();
    Map<Sound, Long> soundTagNumMap = new HashMap<Sound, Long>();

    // fetch tag : sounds map
    for (Tag tag : tags) {
      tagSoundMap.put(tag, tagService.getSoundsWithTag(tag.getLabel()));
    }

    for (Tag tag : tagSoundMap.keySet()) {
      List<Sound> soundsOfTag = tagSoundMap.get(tag);
      for (Sound soundOfTag : soundsOfTag) {
        if (soundTagNumMap.containsKey(soundOfTag)) {
          soundTagNumMap.put(soundOfTag, soundTagNumMap.get(soundOfTag) + 1);
        } else {
          soundTagNumMap.put(soundOfTag, (long) 1);
        }
      }
    }

    List<Sound> allResult =
        SocialUtils.toSeqList(SocialUtils.sortMapByValue(soundTagNumMap, false));
    return allResult;
  }

  @Override
  public List<Sound> getLikedSoundsByUser(String userAlias) throws SoundException {
    List<Sound> sounds = new ArrayList<Sound>();
    List<SoundLike> likes = soundLikeDAO.find("user.profile.alias", userAlias);
    if (likes != null) {
      for (SoundLike like : likes) {
        sounds.add(like.getSound());
      }
    }
    return sounds;
  }

  @Override
  public List<Sound> recommandSoundsForUser(String userAlias, Integer pageNum, Integer pageSize)
      throws SoundException, UserException {
    List<Sound> liked = getLikedSoundsByUser(userAlias);
    Set<Tag> tags = new HashSet<Tag>();
    for (Sound sound : liked) {
      tags.addAll(sound.getTags());
    }

    List<Sound> byTags = recommandSoundsByTags(tags);

    List<Sound> toReturn = SocialUtils.sliceList(byTags, pageNum, pageSize);

    if (toReturn.size() < pageNum) {
      toReturn.addAll(recommandRandomSounds(pageNum - toReturn.size()));
    }

    return toReturn;
  }

  private List<Sound> recommandRandomSounds(int number) {
    return soundDAO.findTopOnes(number, "soundSocial.likesCount");
  }

}

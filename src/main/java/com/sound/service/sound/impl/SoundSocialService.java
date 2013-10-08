package com.sound.service.sound.impl;

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

import com.sound.constant.Constant;
import com.sound.dao.SoundDAO;
import com.sound.dao.UserDAO;
import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.Sound.SoundProfile.SoundPoster;
import com.sound.model.SoundActivity.SoundComment;
import com.sound.model.SoundActivity.SoundLike;
import com.sound.model.SoundActivity.SoundPlay;
import com.sound.model.SoundActivity.SoundRecord;
import com.sound.model.Tag;
import com.sound.model.User;
import com.sound.service.storage.itf.RemoteStorageServiceV2;

@Service
@Scope("singleton")
public class SoundSocialService implements com.sound.service.sound.itf.SoundSocialService {

  @Autowired
  UserDAO userDAO;

  @Autowired
  SoundDAO soundDAO;

  @Autowired
  TagService tagService;

  @Autowired
  RemoteStorageServiceV2 remoteStorageService;

  @Override
  public Map<String, String> play(User user, Sound sound) throws SoundException {
    SoundPlay play = new SoundPlay();
    play.setOwner(user);
    play.setCreatedTime(new Date());
    sound.addPlay(play);
    soundDAO.save(sound);

    Map<String, String> playResult = new HashMap<String, String>();
    playResult.put("played", String.valueOf(sound.getPlays().size()));
    playResult.put("url", remoteStorageService.getDownloadURL(sound.getSoundData().getObjectId(),
        "sound", "avthumb/mp3"));

    return playResult;
  }

  @Override
  public Integer like(User user, Sound sound) throws SoundException {
    // Check if the user has liked the sound
    for (SoundLike like : sound.getLikes()) {
      if (like.getOwner().equals(user)) {
        return sound.getLikes().size();
      }
    }

    SoundLike like = new SoundLike();
    like.setOwner(user);
    like.setCreatedTime(new Date());
    sound.addLike(like);
    soundDAO.save(sound);

    return sound.getLikes().size();
  }

  @Override
  public Integer dislike(User user, Sound sound) throws SoundException {
    // Check if use liked the sound.
    SoundLike liked = null;
    for (SoundLike like : sound.getLikes()) {
      if (like.getOwner().equals(user)) {
        liked = like;
      }
    }
    if (liked != null) {
      sound.removeLike(liked);
      soundDAO.save(sound);
    }

    return sound.getLikes().size();
  }

  @Override
  public Integer repost(User user, Sound sound) throws SoundException {
    // Check if use reposted the sound.
    for (SoundRecord record : sound.getRecords()) {
      if (record.getOwner().equals(user) && record.getType().equals(Constant.SOUND_RECORD_REPOST)) {
        // minus one creation record
        return sound.getRecords().size() - 1;
      }
    }

    SoundRecord record = new SoundRecord();
    record.setOwner(user);
    record.setType(Constant.SOUND_RECORD_REPOST);
    record.setCreatedTime(new Date());
    sound.addRecord(record);
    soundDAO.save(sound);

    // minus one creation record
    return sound.getRecords().size() - 1;
  }

  @Override
  public Integer unrepost(User user, Sound sound) throws SoundException {
    // Check if use liked the sound.
    SoundRecord recorded = null;
    for (SoundRecord oneRecord : sound.getRecords()) {
      if (oneRecord.getOwner().equals(user)
          && oneRecord.getType().equals(Constant.SOUND_RECORD_REPOST)) {
        recorded = oneRecord;
      }
    }
    if (recorded != null) {
      sound.removeRecord(recorded);
      soundDAO.save(sound);
    }

    // minus one creation record
    return sound.getRecords().size() - 1;
  }

  @Override
  public Integer comment(Sound sound, User user, User toUser, String comment, Float pointAt) {
    SoundComment soundComment = new SoundComment();
    soundComment.setCommentId(String.valueOf(System.currentTimeMillis()));
    soundComment.setOwner(user);
    soundComment.setTo(toUser);
    soundComment.setCreatedTime(new Date());
    soundComment.setComment(comment);
    soundComment.setPointAt((null == pointAt || pointAt < 0) ? null : pointAt);

    sound.addComment(soundComment);
    soundDAO.save(sound);

    return sound.getComments().size();
  }

  @Override
  public Integer uncomment(Sound sound, String commentId) throws SoundException {

    SoundComment commentToDelete = null;
    for (SoundComment comment : sound.getComments()) {
      if (comment.getCommentId().equals(commentId)) {
        commentToDelete = comment;
      }
    }

    if (null != commentToDelete) {
      sound.removeComment(commentToDelete);
      soundDAO.save(sound);
    }

    return sound.getComments().size();
  }

  @Override
  public List<SoundComment> getCommentsInsound(Sound sound)
      throws SoundException {
    List<SoundComment> comments = new ArrayList<SoundComment>();
    
    for(SoundComment comment: sound.getComments())
    {
      if (comment.getPointAt() != null && comment.getPointAt() > 0 && null == comment.getTo())
      {
        comments.add(comment);
      }
    }

    for (SoundComment comment : comments) {
      if (comment.getOwner().getProfile().hasAvatar()) {
        comment
            .getOwner()
            .getProfile()
            .setAvatorUrl(
                remoteStorageService.getDownloadURL(comment.getOwner().getId().toString(), "image",
                    "format/png"));
      } else {
        comment.getOwner().getProfile().setAvatorUrl(null);
      }
    }

    return comments;
  }
  
  @Override
  public List<SoundComment> getComments(Sound sound, Integer pageNum, Integer commentsPerPage)
      throws SoundException {
    List<SoundComment> comments = sound.getComments();

    if ((pageNum-1) * commentsPerPage >=  comments.size())
    {
      comments.clear();
    }
    else
    {
      comments = comments.subList((pageNum-1) * commentsPerPage, pageNum * commentsPerPage > comments.size()? comments.size(): pageNum * commentsPerPage);
    }
    for (SoundComment comment : comments) {
      if (comment.getOwner().getProfile().hasAvatar()) {
        comment
            .getOwner()
            .getProfile()
            .setAvatorUrl(
                remoteStorageService.getDownloadURL(comment.getOwner().getId().toString(), "image",
                    "format/png"));
      } else {
        comment.getOwner().getProfile().setAvatorUrl(null);
      }
    }

    return comments;
  }

  @Override
  public List<Sound> recommandSoundsForUser(User recommendTo, Integer pageNum, Integer pageSize) {
    List<Sound> liked = soundDAO.getRecommendSoundsByUser(recommendTo, 0, 50);
    Set<Tag> tags = new HashSet<Tag>();
    for (Sound sound : liked) {
      tags.addAll(sound.getTags());
    }

    for (Tag tag : recommendTo.getTags()) {
      tags.add(tag);
    }

    List<Sound> toReturn =
        soundDAO.getSoundByTags(recommendTo, tags, (pageNum - 1) * pageSize, pageSize);

    if (toReturn.size() < pageNum) {
      toReturn.addAll(recommandRandomSounds(recommendTo, pageNum - toReturn.size()));
    }

    for (Sound sound : toReturn) {
      generateSoundPoster(sound);
    }

    return toReturn;
  }

  private void generateSoundPoster(Sound sound) {
    if (null != sound.getProfile().getPoster()) {
      sound
          .getProfile()
          .getPoster()
          .setUrl(
              remoteStorageService.getDownloadURL(sound.getProfile().getPoster().getPosterId(),
                  "image", "format/png"));
    } else {
      SoundPoster poster = new SoundPoster();
      poster.setUrl("img/voice.jpg");
      sound.getProfile().setPoster(poster);
    }
  }

  private List<Sound> recommandRandomSounds(User recommendTo, int number) {
    Map<String, List<Object>> excludes = new HashMap<String, List<Object>>();
    List<Object> users = new ArrayList<Object>();
    users.add(recommendTo);
    excludes.put("profile.owner", users);
    return soundDAO.findTopOnes(number, excludes);
  }

}

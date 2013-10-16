package com.sound.service.sound.itf;

import java.util.List;
import java.util.Map;

import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.SoundActivity.SoundComment;
import com.sound.model.SoundActivity.SoundLike;
import com.sound.model.SoundActivity.SoundPlay;
import com.sound.model.SoundActivity.SoundRecord;
import com.sound.model.SoundActivity.SoundVisit;
import com.sound.model.User;

public interface SoundSocialService {

  public Map<String, String> play(User user, Sound sound) throws SoundException;
  
  public List<SoundPlay> getPlayed(Sound sound, Integer pageNum, Integer perPage) throws SoundException;

  public Integer like(User user, Sound sound) throws SoundException;

  public Integer dislike(User user, Sound sound) throws SoundException;
  
  public List<SoundLike> getLiked(Sound sound, Integer pageNum, Integer perPage) throws SoundException;

  public Integer repost(User user, Sound sound) throws SoundException;

  public Integer unrepost(User user, Sound sound) throws SoundException;
  
  public List<SoundRecord> getReposts(Sound sound, Integer pageNum, Integer perPage) throws SoundException;

  public Integer comment(Sound sound, User user, User toUser, String comment, Float pointAt);

  public Integer uncomment(Sound sound, String commentId) throws SoundException;

  public List<SoundComment> getComments(Sound sound, Integer pageNum, Integer soundsPerPage)
      throws SoundException;

  public List<Sound> recommandSoundsForUser(User recommendTo, Integer pageNum, Integer pageSize);

  List<SoundComment> getCommentsInsound(Sound sound) throws SoundException;

  public void addVisit(Sound sound, User user);
  
  public List<SoundVisit> getVisits(Sound sound, Integer pageNum, Integer perPage) throws SoundException;
}

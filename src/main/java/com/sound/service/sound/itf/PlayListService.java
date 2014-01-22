package com.sound.service.sound.itf;

import java.util.List;

import com.sound.model.Sound;
import com.sound.model.User;

public interface PlayListService {
  public void addPlayRecord(User user, Sound sound);
  
  public List<Sound> getPlayRecords(User user);
  
  public void removePlayRecord(User user, Sound sound);
}

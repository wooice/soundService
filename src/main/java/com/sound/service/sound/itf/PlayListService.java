package com.sound.service.sound.itf;

import java.util.List;

import com.sound.model.Sound;
import com.sound.model.User;

public interface PlayListService {
  public void add(User user, Sound sound);
  
  public List<Sound> list(User user, Integer pageNum, Integer perPage);
  
  public void remove(User user, Sound sound);
  
  public void updateStatus(User user, Sound sound, String status);
}

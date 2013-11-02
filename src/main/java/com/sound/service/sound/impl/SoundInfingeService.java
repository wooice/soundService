package com.sound.service.sound.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sound.dao.SoundInfingeDAO;
import com.sound.model.SoundInfringe;

@Service
@Scope("singleton")
public class SoundInfingeService implements com.sound.service.sound.itf.SoundInfingeService {
  @Autowired
  SoundInfingeDAO soundInfingeDAO;
  
  @Override
  public void create(SoundInfringe soundInfringe) {
    soundInfingeDAO.save(soundInfringe);
  }

  @Override
  public List<SoundInfringe> list(String status, int pageNum, int perPage) {
    Map<String, Object> cratiaries = new HashMap<String, Object>();
    return soundInfingeDAO.find(cratiaries);
  }

}

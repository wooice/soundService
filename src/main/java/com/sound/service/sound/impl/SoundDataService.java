package com.sound.service.sound.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sound.dao.SoundDataDAO;
import com.sound.model.Sound.SoundData;
import com.sound.model.User;

@Service
@Scope("singleton")
public class SoundDataService implements com.sound.service.sound.itf.SoundDataService {
  @Autowired
  SoundDataDAO soundDataDAO;

  public SoundData save(SoundData soundData) {
    soundDataDAO.save(soundData);

    return soundData;
  }

  public SoundData load(String objectId) {
    return soundDataDAO.findOne("objectId", objectId);
  }

  public void delete(String objectId) {
    soundDataDAO.deleteByProperty("objectId", objectId);
  }

  public SoundData update(SoundData soundData) {
    soundDataDAO.updateProperty("objectId", soundData.getObjectId(), "duration",
        soundData.getDuration());
    soundDataDAO.updateProperty("objectId", soundData.getObjectId(), "originName",
        soundData.getOriginName());
    soundDataDAO.updateProperty("objectId", soundData.getObjectId(), "wave", soundData.getWave());
    return soundData;
  }

  @Override
  public List<SoundData> loadDataByOwner(User owner) {
    return soundDataDAO.find("owner", owner);
  }

}

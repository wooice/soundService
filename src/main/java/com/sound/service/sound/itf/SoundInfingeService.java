package com.sound.service.sound.itf;

import java.util.List;

import com.sound.model.SoundInfringe;

public interface SoundInfingeService {

  public void create(SoundInfringe soundInfringe);
  
  public List<SoundInfringe> list(String status, int pageNum, int perPage);
}

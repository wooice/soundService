package com.sound.service.endpoint;

import java.util.ArrayList;
import java.util.List;

import com.sound.service.sound.itf.SoundEventListener;

public class BaseEndpoint {

  List<SoundEventListener> listeners = new ArrayList<SoundEventListener>();

  protected void addEventListener(SoundEventListener listner) {
    listeners.add(listner);
  }
}

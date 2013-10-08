package com.sound.service.sound.itf;

import java.util.EventListener;
import java.util.EventObject;

public interface SoundEventListener extends EventListener {

  public void notify(EventObject event);

}

package com.sound.service.sound.itf;

import java.util.List;

import com.sound.exception.SoundAuthException;
import com.sound.exception.SoundException;
import com.sound.model.Sound.QueueNode;
import com.sound.model.SoundLocal;
import com.sound.model.User;
import com.sound.processor.exception.AudioProcessException;

public interface QueueService {

  public SoundLocal processSound(User user, String soundUrl, QueueNode node) throws SoundException,
      SoundAuthException, AudioProcessException;

  public void enqueue(QueueNode node);

  public List<QueueNode> listQueue();

  public void dequeue(QueueNode node);
}

package com.sound.model.file;

import java.io.InputStream;

import com.sound.processor.model.Wave;

public class SoundLocal {

  private String originName;

  private String fileName;

  private Wave wave;

  private InputStream soundStream;

  private Long length;

  private Long duration;

  public String getOriginName() {
    return originName;
  }

  public void setOriginName(String originName) {
    this.originName = originName;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public Wave getWave() {
    return wave;
  }

  public void setWave(Wave wave) {
    this.wave = wave;
  }

  public InputStream getSoundStream() {
    return soundStream;
  }

  public void setSoundStream(InputStream soundStream) {
    this.soundStream = soundStream;
  }

  public Long getLength() {
    return length;
  }

  public void setLength(Long length) {
    this.length = length;
  }

  public Long getDuration() {
    return duration;
  }

  public void setDuration(Long duration) {
    this.duration = duration;
  }

}

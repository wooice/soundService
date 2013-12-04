package com.sound.model;

import java.io.InputStream;

import com.sound.model.Sound.SoundFormat;

public class SoundLocal {

  private String originName;

  private String fileName;

  private InputStream soundStream;

  private Long length;

  private SoundFormat soundFormat;

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

  public SoundFormat getSoundFormat() {
    return soundFormat;
  }

  public void setSoundFormat(SoundFormat soundFormat) {
    this.soundFormat = soundFormat;
  }
 
}

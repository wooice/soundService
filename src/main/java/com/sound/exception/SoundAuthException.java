package com.sound.exception;

public class SoundAuthException extends SoundException{
  
  private static final long serialVersionUID = 1L;
  
  private String album;
  
  private String albumArtists;
  
  private String artists;
  
  private String composer;

  public SoundAuthException() {
    super();
  }
  
  public SoundAuthException(String albumArtists, String artists, String composer) {
    super();
    this.albumArtists = albumArtists;
    this.artists = artists;
    this.composer = composer;
  }

  public String getAlbum() {
    return album;
  }

  public void setAlbum(String album) {
    this.album = album;
  }

  public String getAlbumArtists() {
    return albumArtists;
  }

  public void setAlbumArtists(String albumArtists) {
    this.albumArtists = albumArtists;
  }

  public String getArtists() {
    return artists;
  }

  public void setArtists(String artists) {
    this.artists = artists;
  }

  public String getComposer() {
    return composer;
  }

  public void setComposer(String composer) {
    this.composer = composer;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }
  
}

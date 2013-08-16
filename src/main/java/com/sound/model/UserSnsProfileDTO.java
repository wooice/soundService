package com.sound.model;

public class UserSnsProfileDTO extends BaseModel {
  private String website;
  private String sina;
  private String qq;
  private String renren;
  private String douban;

  public String getWebsite() {
    return website;
  }

  public void setWebsite(String website) {
    this.website = website;
  }

  public String getSina() {
    return sina;
  }

  public void setSina(String sina) {
    this.sina = sina;
  }

  public String getQq() {
    return qq;
  }

  public void setQq(String qq) {
    this.qq = qq;
  }

  public String getRenren() {
    return renren;
  }

  public void setRenren(String renren) {
    this.renren = renren;
  }

  public String getDouban() {
    return douban;
  }

  public void setDouban(String douban) {
    this.douban = douban;
  }

}

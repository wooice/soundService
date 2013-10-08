package com.sound.model;

public class RequestModel {

  public static class commentRequest {
    private String comment;
    private Float pointAt;
    private String toUserAlias;

    public String getComment() {
      return comment;
    }

    public void setComment(String comment) {
      this.comment = comment;
    }

    public Float getPointAt() {
      return pointAt;
    }

    public void setPointAt(Float pointAt) {
      this.pointAt = pointAt;
    }

    public String getToUserAlias() {
      return toUserAlias;
    }

    public void setToUserAlias(String toUserAlias) {
      this.toUserAlias = toUserAlias;
    }
  }

}

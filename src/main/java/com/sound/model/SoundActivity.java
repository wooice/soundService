package com.sound.model;

import java.util.Date;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.github.jmkgreen.morphia.annotations.Reference;
import com.sound.jackson.extension.DateSerializer;

public class SoundActivity {

  @Reference
  protected User owner;

  protected Date createdTime;

  public User getOwner() {
    return owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }

  @JsonSerialize(using = DateSerializer.class)
  public Date getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(Date createdTime) {
    this.createdTime = createdTime;
  }

  public static class SoundComment extends SoundActivity {
    private String commentId;

    private String comment;

    private Float pointAt;

    @Reference
    private User to;

    public String getCommentId() {
      return commentId;
    }

    public void setCommentId(String commentId) {
      this.commentId = commentId;
    }

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

    public User getTo() {
      return to;
    }

    public void setTo(User to) {
      this.to = to;
    }

  }

  public static class SoundLike extends SoundActivity {}

  public static class SoundPlay extends SoundActivity {}

  public static class SoundRecord extends SoundActivity {
    private String type;

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }
  }

  public static class SoundShare extends SoundActivity {}

  public static class SoundVisit extends SoundActivity {}

}

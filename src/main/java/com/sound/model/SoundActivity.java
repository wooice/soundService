package com.sound.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.Reference;
import com.sound.jackson.extension.DateSerializer;
import com.sound.jackson.extension.IdSerializer;

public class SoundActivity {

  @Id
  private ObjectId id;

  @Reference
  protected Sound sound;

  @Reference
  protected User owner;

  protected Date createdTime;

  @JsonSerialize(using = IdSerializer.class)
  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public Sound getSound() {
    return sound;
  }

  public void setSound(Sound sound) {
    this.sound = sound;
  }

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

  @Entity
  public static class SoundComment extends SoundActivity {
    private String comment;

    private Float pointAt;

    @Reference
    private User to;

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

  @Entity
  public static class SoundLike extends SoundActivity {}

  @Entity
  public static class SoundPlay extends SoundActivity {}

  @Entity
  public static class SoundRecord extends SoundActivity {
    public static final String CREATE = "create";

    public static final String REPOST = "repost";

    private List<String> actions;

    public List<String> getActions() {
      return actions;
    }

    public void setActions(List<String> actions) {
      this.actions = actions;
    }

    public void addAction(String action) {
      if (null == actions) {
        actions = new ArrayList<String>();
      }
      if (!actions.contains(action)) {
        actions.add(action);
      }
    }

    public void removeAction(String action) {
      if (null == actions) {
        actions = new ArrayList<String>();
      }
      if (actions.contains(action)) {
        actions.remove(action);
      }
    }

    public boolean hasAction(String action) {
      if (null == actions) {
        actions = new ArrayList<String>();
      }

      return actions.contains(action);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((sound == null) ? 0 : sound.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      SoundRecord other = (SoundRecord) obj;
      if (sound == null) {
        if (other.sound != null) return false;
      } else if (!sound.equals(other.sound)) return false;
      return true;
    }

  }

  @Entity
  public static class SoundShare extends SoundActivity {}
}

package com.sound.model;

import java.util.Date;

import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.Reference;

public class UserActivity {

  @Id
  private ObjectId id;

  @Reference
  private User fromUser;

  private Date createdTime;

  @Entity
  public static class UserConnect extends UserActivity {
    @Reference
    private User toUser;

    public User getToUser() {
      return toUser;
    }

    public void setToUser(User toUser) {
      this.toUser = toUser;
    }
  }

  @JsonIgnore
  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public User getFromUser() {
    return fromUser;
  }

  public void setFromUser(User fromUser) {
    this.fromUser = fromUser;
  }

  public Date getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(Date createdTime) {
    this.createdTime = createdTime;
  }
}

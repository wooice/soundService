package com.sound.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.Reference;
import com.sound.jackson.extension.DateSerializer;
import com.sound.jackson.extension.IdSerializer;

@Entity(noClassnameStored = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserMessage extends BaseModel {
  @Id
  private ObjectId id;

  @Reference
  private User from;
  
  /* unread  read deleted */
  private String fromStatus;

  @Reference
  private User to;

  /* unread  read deleted */
  private String toStatus;
  
  private String topic;
  private String content;
  private Date date;
  private Date updatedDate;
  
  @JsonIgnore
  @Embedded
  List<UserMessage> replies = new ArrayList<UserMessage>();

  @JsonSerialize(using = IdSerializer.class)
  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }
  
  public UserMessage getLastReply()
  {
    if (replies.size() == 0)
    {
      return null;
    }
    
    return replies.get(replies.size()-1);
  }

  public User getFrom() {
    return from;
  }

  public void setFrom(User from) {
    this.from = from;
  }

  public User getTo() {
    return to;
  }

  public void setTo(User to) {
    this.to = to;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }
  
  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getSummary() {
    return content.length() <= 30 ? content : content.substring(0, 29) + "...";
  }

  @JsonSerialize(using = DateSerializer.class)
  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }
 
  public String getFromStatus() {
    return fromStatus;
  }

  public void setFromStatus(String fromStatus) {
    this.fromStatus = fromStatus;
  }

  public String getToStatus() {
    return toStatus;
  }

  public void setToStatus(String toStatus) {
    this.toStatus = toStatus;
  }

  public List<UserMessage> getReplies() {
    return replies;
  }

  public void setReplies(List<UserMessage> replies) {
    this.replies = replies;
  }

  public Date getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = updatedDate;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((from == null) ? 0 : from.hashCode());
    result = prime * result + ((to == null) ? 0 : to.hashCode());
    result = prime * result + ((date == null) ? 0 : date.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    UserMessage other = (UserMessage) obj;
    if (other.hashCode() == hashCode())
      return true;
    else
      return false;
  }

}

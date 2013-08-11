package com.sound.model;

import java.util.Date;

import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.Reference;

@Entity(noClassnameStored = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tag extends BaseModel {
  @Id
  private ObjectId id;

  private boolean curated;

  private String label;

  private Date createdDate;

  @Reference(lazy = true)
  private User createdUser;

  @Reference(lazy = true)
  private TagCategory category;

  @JsonIgnore
  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public boolean isCurated() {
    return curated;
  }

  public void setCurated(boolean curated) {
    this.curated = curated;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public User getCreatedUser() {
    return createdUser;
  }

  public void setCreatedUser(User createdUser) {
    this.createdUser = createdUser;
  }

  public TagCategory getCategory() {
    return category;
  }

  public void setCategory(TagCategory category) {
    this.category = category;
  }

  @Entity(noClassnameStored = true)
  public static class TagCategory {
    @Id
    private ObjectId id;

    private String name;

    private Date createdTime;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Date getCreatedTime() {
      return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
      this.createdTime = createdTime;
    }

    @JsonIgnore
    public ObjectId getId() {
      return id;
    }

    public void setId(ObjectId id) {
      this.id = id;
    }

  }

  @Override
  public int hashCode() {
    return label.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Tag) {
      return ((Tag) o).getLabel().equals(label);
    }
    return false;
  }
}

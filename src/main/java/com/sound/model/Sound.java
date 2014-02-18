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
import com.github.jmkgreen.morphia.annotations.Transient;
import com.sound.constant.Constant;
import com.sound.jackson.extension.DateSerializer;
import com.sound.jackson.extension.IdSerializer;
import com.sound.model.SoundActivity.SoundComment;
import com.sound.model.SoundActivity.SoundLike;
import com.sound.model.SoundActivity.SoundPlay;
import com.sound.model.SoundActivity.SoundRecord;
import com.sound.model.SoundActivity.SoundReport;
import com.sound.model.SoundActivity.SoundVisit;
import com.sound.model.enums.SoundState;
import com.sound.model.enums.SoundType;

@Entity(noClassnameStored = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Sound extends BaseModel {
  @Id
  private ObjectId id;

  @Embedded
  private SoundProfile profile;

  @Reference(lazy = true)
  private List<Sound> innerSounds;

  @Reference(lazy = true)
  private List<Sound> sets;

  @Reference(lazy = true)
  private List<Tag> tags;

  @Embedded(concreteClass = java.util.ArrayList.class)
  private List<SoundVisit> visits = new ArrayList<SoundVisit>();

  @Embedded(concreteClass = java.util.ArrayList.class)
  private List<SoundComment> comments = new ArrayList<SoundComment>();
  
  @Embedded(concreteClass = java.util.ArrayList.class)
  private List<SoundLike> likes = new ArrayList<SoundLike>();
  
  @Embedded(concreteClass = java.util.ArrayList.class)
  private List<SoundPlay> plays = new ArrayList<SoundPlay>();
  
  @Embedded(concreteClass = java.util.ArrayList.class)
  private List<SoundRecord> records = new ArrayList<SoundRecord>();
  
  @Embedded(concreteClass = java.util.ArrayList.class)
  private List<SoundReport> reports = new ArrayList<SoundReport>();
  
  @Transient
  private SoundSocial soundSocial;

  @Transient
  private UserPrefer userPrefer;

  @JsonSerialize(using = IdSerializer.class)
  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }
  
  public SoundProfile getProfile() {
    return profile;
  }

  public void setProfile(SoundProfile profile) {
    this.profile = profile;
  }

  public SoundSocial getSoundSocial() {
    return soundSocial;
  }

  public void setSoundSocial(SoundSocial soundSocial) {
    this.soundSocial = soundSocial;
  }

  public List<Sound> getInnerSounds() {
    return innerSounds;
  }

  public void setInnerSounds(List<Sound> innerSounds) {
    this.innerSounds = innerSounds;
  }

  public List<Sound> getSets() {
    return sets;
  }

  public void setSets(List<Sound> sets) {
    this.sets = sets;
  }

  public List<Tag> getTags() {
    return tags;
  }

  public void setTags(List<Tag> tags) {
    this.tags = tags;
  }

  public void addTags(List<Tag> tags) {
    if (this.tags == null) {
      this.tags = new ArrayList<Tag>();
    }
    for (Tag newTag : tags) {
      if (!this.tags.contains(newTag)) {
        this.tags.addAll(tags);
      }
    }
  }
 
  public List<SoundVisit> getVisits() {
    return visits;
  }

  public void setVisits(List<SoundVisit> visits) {
    this.visits = visits;
  }

  public UserPrefer getUserPrefer() {
    return userPrefer;
  }

  public void setUserPrefer(UserPrefer userPrefer) {
    this.userPrefer = userPrefer;
  }

  @JsonIgnore
  public List<SoundComment> getComments() {
    return comments;
  }

  public void setComments(List<SoundComment> comments) {
    this.comments = comments;
  }
  
  public void addComment(SoundComment comment)
  {
    this.comments.add(comment);
  }
  
  public void removeComment(SoundComment comment)
  {
    this.comments.remove(comment);
  }

  @JsonIgnore
  public List<SoundLike> getLikes() {
    return likes;
  }

  public void setLikes(List<SoundLike> likes) {
    this.likes = likes;
  }

  public void addLike(SoundLike like)
  {
    this.likes.add(like);
  }
  
  public void removeLike(SoundLike like)
  {
    this.likes.remove(like);
  }
  
  @JsonIgnore
  public List<SoundPlay> getPlays() {
    return plays;
  }

  public void setPlays(List<SoundPlay> plays) {
    this.plays = plays;
  }

  public void addPlay(SoundPlay play)
  {
    this.plays.add(play);
  }
  
  @JsonIgnore
  public List<SoundRecord> getRecords() {
    return this.records;
  }
  
  public List<SoundRecord> getReposts() {
    List<SoundRecord> reposts = new ArrayList<SoundRecord>();
    
    for (SoundRecord record: this.records)
    {
      if (record.getType().equals(Constant.SOUND_RECORD_REPOST))
      {
        reposts.add(record);
      }
    }
    return reposts;
  }

  public void setRecords(List<SoundRecord> records) {
    this.records = records;
  }
  
  public void addRecord(SoundRecord record)
  {
    this.records.add(record);
  }
  
  public void removeRecord(SoundRecord record)
  {
    this.records.remove(record);
  }

  @JsonIgnore
  public List<SoundReport> getReports() {
    return reports;
  }

  public void setReports(List<SoundReport> reports) {
    this.reports = reports;
  }

  public void addReport(SoundReport report){
     for (SoundReport oneReport: this.reports)
     {
       if (oneReport.getOwner().equals(report.getOwner()))
       {
         return;
       }
     }
     this.reports.add(report);
  }

  /**
   * @author xduo
   *
   */
  /**
   * @author xduo
   *
   */
  public static class SoundProfile {
    @Reference(lazy = true)
    private User owner;

    @Embedded
    private SoundPoster poster;

    private String name;

    private String alias;

    private String description;

    /**
     * published, private, deleted. refer to SoundState.
     */
    private int status;

    private String remoteId;

    private String extension;
    /**
     * sound, set
     */
    private int type;

    private Date createdTime;

    private Date modifiedTime;

    /**
     * private, public, closed 
     */
    private String commentMode;
    
    /**
     * resing, original
     */
    private String recordType;
    
    @Embedded
    private SoundRight soundRight;
    
    private int priority = 0;
    
    private Date priorityUpdatedDate;
    
    private boolean downloadable;
    
    private float duration = 0;
    
    private boolean processed = false;
    
    private String url;
    
    private Date urlGeneratedDate;
    
    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }
    
    public Date getUrlGeneratedDate() {
      return urlGeneratedDate;
    }

    public void setUrlGeneratedDate(Date urlGeneratedDate) {
      this.urlGeneratedDate = urlGeneratedDate;
    }

    @JsonIgnore
    public String getRecordType() {
      return recordType;
    }

    public void setRecordType(String recordType) {
      this.recordType = recordType;
    }

    public SoundRight getSoundRight() {
      return soundRight;
    }

    public void setSoundRight(SoundRight soundRight) {
      this.soundRight = soundRight;
    }

    public int getPriority() {
      return priority;
    }

    public void setPriority(int priority) {
      this.priority = priority;
    }

    public Date getPriorityUpdatedDate() {
      return priorityUpdatedDate;
    }

    public void setPriorityUpdatedDate(Date priorityUpdatedDate) {
      this.priorityUpdatedDate = priorityUpdatedDate;
    }

    public User getOwner() {
      return owner;
    }

    public void setOwner(User owner) {
      this.owner = owner;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }


    public String getAlias() {
      return alias;
    }

    public void setAlias(String alias) {
      this.alias = alias;
    }

    public String getStatus() {
      return SoundState.getStateName(status);
    }

    public void setStatus(String status) {
      this.status = SoundState.getStateId(status);
    }

    public String getRemoteId() {
      return remoteId;
    }

    public void setRemoteId(String remoteId) {
      this.remoteId = remoteId;
    }

    public String getCommentMode() {
      return commentMode;
    }

    public void setCommentMode(String commentMode) {
      this.commentMode = commentMode;
    }

    public String getExtension() {
      return extension;
    }

    public void setExtension(String extension) {
      this.extension = extension;
    }

    public String getType() {
      return SoundType.getTypeName(this.type);
    }

    public void setType(String type) {
      this.type = SoundType.getTypeId(type);
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    @JsonSerialize(using = DateSerializer.class)
    public Date getCreatedTime() {
      return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
      this.createdTime = createdTime;
    }

    public Date getModifiedTime() {
      return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
      this.modifiedTime = modifiedTime;
    }

    public SoundPoster getPoster() {
      return poster;
    }

    public void setPoster(SoundPoster poster) {
      this.poster = poster;
    }

    public boolean isDownloadable() {
      return downloadable;
    }

    public void setDownloadable(boolean downloadable) {
      this.downloadable = downloadable;
    }
    
    public float getDuration() {
      return duration;
    }

    public void setDuration(float duration) {
      this.duration = duration;
    }
    
    public boolean isProcessed() {
      return processed;
    }

    public void setProcessed(boolean processed) {
      this.processed = processed;
    }

    public static class SoundPoster {

      private String extension;

      private String url;

      public String getExtension() {
        return extension;
      }

      public void setExtension(String extension) {
        this.extension = extension;
      }

      public String getUrl() {
        return url;
      }

      public void setUrl(String url) {
        this.url = url;
      }

    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      SoundProfile other = (SoundProfile) obj;
      if (name == null) {
        if (other.name != null) return false;
      } else if (!name.equals(other.name)) return false;
      return true;
    }

  }
  
  public static class SoundRight {
    private byte[] rightCopy;
    
    private String rightNumber;

    public byte[] getRightCopy() {
      return rightCopy;
    }

    public void setRightCopy(byte[] rightCopy) {
      this.rightCopy = rightCopy;
    }

    public String getRightNumber() {
      return rightNumber;
    }

    public void setRightNumber(String rightNumber) {
      this.rightNumber = rightNumber;
    }
  }

  public static class SoundSocial {
    private Integer playedCount;

    private Integer likesCount;

    private Integer reportsCount;

    private Integer commentsCount;
    
    private Integer visitsCount;

    public SoundSocial() {
      playedCount = 0;
      likesCount = 0;
      reportsCount = 0;
      commentsCount = 0;
      visitsCount = 0;
    }

    public Integer getPlayedCount() {
      return playedCount;
    }

    public void setPlayedCount(Integer playedCount) {
      this.playedCount = playedCount;
    }

    public Integer getLikesCount() {
      return likesCount;
    }

    public void setLikesCount(Integer likesCount) {
      this.likesCount = likesCount;
    }

    public Integer getReportsCount() {
      return reportsCount;
    }

    public void setReportsCount(Integer reportsCount) {
      this.reportsCount = reportsCount;
    }

    public Integer getCommentsCount() {
      return commentsCount;
    }

    public void setCommentsCount(Integer commentsCount) {
      this.commentsCount = commentsCount;
    }

    public Integer getVisitsCount() {
      return visitsCount;
    }

    public void setVisitsCount(Integer visitsCount) {
      this.visitsCount = visitsCount;
    }

  }

  public static class UserPrefer {

    private Integer like;

    private Integer repost;

    public Integer getLike() {
      return like;
    }

    public void setLike(Integer like) {
      this.like = like;
    }

    public Integer getRepost() {
      return repost;
    }

    public void setRepost(Integer repost) {
      this.repost = repost;
    }
  }

  @Entity(noClassnameStored = true)
  public static class QueueNode {
    @Id
    private ObjectId id;

    private String originFileName;

    private String fileName;

    @Reference
    private User owner;

    private Date createdDate;
    
    /* live, processed, deleted */
    private String status;

    @JsonIgnore
    public ObjectId getId() {
      return id;
    }

    public void setId(ObjectId id) {
      this.id = id;
    }

    public String getOriginFileName() {
      return originFileName;
    }

    public void setOriginFileName(String originFileName) {
      this.originFileName = originFileName;
    }

    public String getFileName() {
      return fileName;
    }

    public void setFileName(String fileName) {
      this.fileName = fileName;
    }

    public User getOwner() {
      return owner;
    }

    public void setOwner(User owner) {
      this.owner = owner;
    }

    public Date getCreatedDate() {
      return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
      this.createdDate = createdDate;
    }

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }
  }
  
  public static class SoundFormat {
    private float duration;
    private String performer;
    private int track;
    private String title;
    private String genre;
    private String publisher;
    private String composer;
    private String artist;
    private String album_artist;
    
    public float getDuration() {
      return duration;
    }
    public void setDuration(float duration) {
      this.duration = duration;
    }
    public String getPerformer() {
      return performer;
    }
    public void setPerformer(String performer) {
      this.performer = performer;
    }
    public int getTrack() {
      return track;
    }
    public void setTrack(int track) {
      this.track = track;
    }
    public String getTitle() {
      return title;
    }
    public void setTitle(String title) {
      this.title = title;
    }
    public String getGenre() {
      return genre;
    }
    public void setGenre(String genre) {
      this.genre = genre;
    }
    public String getPublisher() {
      return publisher;
    }
    public void setPublisher(String publisher) {
      this.publisher = publisher;
    }
    public String getComposer() {
      return composer;
    }
    public void setComposer(String composer) {
      this.composer = composer;
    }
    public String getArtist() {
      return artist;
    }
    public void setArtist(String artist) {
      this.artist = artist;
    }
    public String getAlbum_artist() {
      return album_artist;
    }
    public void setAlbum_artist(String album_artist) {
      this.album_artist = album_artist;
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((profile.getAlias() == null) ? 0 : profile.getAlias().hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof Sound)) return false;
    Sound other = (Sound) obj;
    if (profile.getAlias() == null) {
      if (other.profile.getAlias() != null) return false;
    } else if (!profile.getAlias().equals(other.getProfile().getAlias())) return false;
    return true;
  }

}

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
import com.sound.jackson.extension.IdSerializer;
import com.sound.model.enums.GenderEnum;
import com.sound.model.enums.UserOccupationType;
import com.sound.model.enums.UserRoleEnum;

@Entity(noClassnameStored = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends BaseModel {

  @Id
  private ObjectId id;

  @Embedded
  private UserProfile profile;

  @Embedded
  private UserExternal external;

  @Embedded
  private List<UserEmail> emails;
  
  @Reference(lazy = true)
  @JsonIgnore
  private UserAuth auth;

  @Reference(lazy = true)
  private List<Group> groups;

  @Embedded
  private UserSocial userSocial;

  // One user one role. List for future extension.
  @Embedded
  private List<UserRole> userRoles = new ArrayList<UserRole>();
  
  //tags interested
  @Reference(lazy = true)
  private List<Tag> tags = new ArrayList<Tag>();

  @Transient
  private UserPrefer userPrefer;
  
  @Transient
  private String authToken;

  @JsonSerialize(using = IdSerializer.class)
  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }

  public UserProfile getProfile() {
    return profile;
  }

  public void setProfile(UserProfile profile) {
    this.profile = profile;
  }

  public String getAuthToken() {
    return authToken;
  }

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  @JsonIgnore
  public List<Tag> getTags() {
    return tags;
  }

  public void setTags(List<Tag> tags) {
    this.tags = tags;
  }
  
  public boolean containTag(Tag newTag) {
    for (Tag tag: tags)
    {
      if (tag.equals(newTag))
      {
        return true;
      }
    }

    return false;
  }

  public void addTags(List<Tag> tags) {
    for (Tag newTag : tags) {
      if (!this.tags.contains(newTag)) {
        this.tags.addAll(tags);
      }
    }
  }
  
  @JsonIgnore
  public List<Group> getGroups() {
    return groups;
  }

  public void setGroups(List<Group> groups) {
    this.groups = groups;
  }

  public UserExternal getExternal() {
    return external;
  }

  public UserSocial getUserSocial() {
    return userSocial;
  }

  public void setUserSocial(UserSocial social) {
    this.userSocial = social;
  }

  public List<UserRole> getUserRoles() {
    return userRoles;
  }

  public void setUserRoles(List<UserRole> userRoles) {
    this.userRoles = userRoles;
  }

  public void setExternal(UserExternal external) {
    this.external = external;
  }

  @JsonIgnore
  public List<UserEmail> getEmails() {
    return emails;
  }

  public void setEmails(List<UserEmail> emails) {
    this.emails = emails;
  }

  public void addEmail(UserEmail email) {
    this.emails = (null == this.emails) ? new ArrayList<UserEmail>() : this.emails;
    for (UserEmail oneEmail: this.emails)
    {
      if (oneEmail.equals(email))
      {
        return;
      }
    }
    this.emails.add(email);
  }

  public UserAuth getAuth() {
    return auth;
  }

  public void setAuth(UserAuth auth) {
    this.auth = auth;
  }


  public UserPrefer getUserPrefer() {
    return userPrefer;
  }

  public void setUserPrefer(UserPrefer userPrefer) {
    this.userPrefer = userPrefer;
  }

  public void addGroup(Group group) {
    this.groups = (null == this.groups) ? new ArrayList<Group>() : this.groups;

    for (Group oneGroup : this.groups) {
      if (oneGroup == group) {
        return;
      }
    }
    this.groups.add(group);
  }

  public void removeGroup(Group group) {
    this.groups = (null == this.groups) ? new ArrayList<Group>() : this.groups;
    this.groups.remove(group);
  }

  @Entity
  public static class UserProfile {
    private String avatorUrl;
    private String alias;
    private String firstName;
    private String lastName;
    private String city;
    private String country;
    private String description;
    private int age;
    private Boolean gender = null;
    private boolean hasAvatar = false;
    private Date createDate;
    private Color color;
    private List<Integer> occupations = new ArrayList<Integer>();

    public List<UserOccupationType> getOccupationTypes() {
      return UserOccupationType.getTypesByIds(occupations);
    }

    public List<Integer> getOccupations() {
      return this.occupations;
    }

    public void setOccupations(List<Integer> occupations) {
      this.occupations = occupations;
    }

    public String getAvatorUrl() {
      return avatorUrl;
    }

    public void setAvatorUrl(String avatorUrl) {
      this.avatorUrl = avatorUrl;
    }

    public Date getCreateDate() {
      return createDate;
    }

    public void setCreateDate(Date createDate) {
      this.createDate = createDate;
    }

    public String getAlias() {
      return alias;
    }

    public void setAlias(String alias) {
      this.alias = alias;
    }

    public String getFirstName() {
      return firstName;
    }

    public void setFirstName(String firstName) {
      this.firstName = firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public void setLastName(String lastName) {
      this.lastName = lastName;
    }

    public String getCity() {
      return city;
    }

    public void setCity(String city) {
      this.city = city;
    }

    public String getCountry() {
      return country;
    }

    public void setCountry(String country) {
      this.country = country;
    }

    public int getAge() {
      return age;
    }

    public Color getColor() {
      return color;
    }

    public void setColor(Color color) {
      this.color = color;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public void setAge(int age) {
      this.age = age;
    }

    public String getGender() {
      return GenderEnum.getGenderName(gender);
    }

    public void setGender(String gender) {
      this.gender = GenderEnum.getGenderValue(gender);
    }

    public boolean hasAvatar() {
      return hasAvatar;
    }

    public void setHasAvatar(boolean hasAvatar) {
      this.hasAvatar = hasAvatar;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((alias == null) ? 0 : alias.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      UserProfile other = (UserProfile) obj;
      if (alias == null) {
        if (other.alias != null) return false;
      } else if (!alias.equals(other.alias)) return false;
      return true;
    }

    public static class Color {
      private String upper;
      private String lower;
      private String deeper;

      public String getUpper() {
        return upper;
      }

      public void setUpper(String upper) {
        this.upper = upper;
      }

      public String getLower() {
        return lower;
      }

      public void setLower(String lower) {
        this.lower = lower;
      }

      public String getDeeper() {
        return deeper;
      }

      public void setDeeper(String deeper) {
        this.deeper = deeper;
      }

    }
  }

  @Entity
  public static class UserSocial {
    private Long following;

    private Long followed;

    private Long sounds;

    private Long reposts;

    //second
    private Long soundDuration;

    private Long inputMessages;

    private Long outputMessages;

    public Long getFollowing() {
      return following;
    }

    public void setFollowing(Long following) {
      this.following = following;
    }

    public Long getFollowed() {
      return followed;
    }

    public void setFollowed(Long followed) {
      this.followed = followed;
    }

    public Long getSounds() {
      return sounds;
    }

    public void setSounds(Long sounds) {
      this.sounds = sounds;
    }

    public Long getReposts() {
      return reposts;
    }

    public void setReposts(Long reposts) {
      this.reposts = reposts;
    }

    public Long getSoundDuration() {
      return soundDuration;
    }

    public void setSoundDuration(Long soundDuration) {
      this.soundDuration = soundDuration;
    }

    public Long getInputMessages() {
      return inputMessages;
    }

    public void setInputMessages(Long inputMessages) {
      this.inputMessages = inputMessages;
    }

    public Long getOutputMessages() {
      return outputMessages;
    }

    public void setOutputMessages(Long outputMessages) {
      this.outputMessages = outputMessages;
    }
  }

  @Entity
  public static class UserExternal {

    @Embedded
    List<Site> sites = new ArrayList<Site>();

    public List<Site> getSites() {
      return sites;
    }

    public void setSites(List<Site> sites) {
      this.sites = sites;
    }

    public void addSite(Site site) {
      for (Site oneExternal : this.sites) {
        if (oneExternal.equals(site)) {
          return;
        }
      }
      sites.add(site);
    }
    
    public void updateSite(Site site)
    {
      for (int i=0; i<sites.size(); i++) {
        if (sites.get(i).equals(site)) {
          sites.set(i, site);
          return;
        }
      }
      sites.add(site);
    }

    public static class Site {
      String name;

      @JsonIgnore
      String displayName;

      String url;
      
      String uid;
      
      String userName;

      @JsonIgnore
      boolean userCreated = false;
      
      @JsonIgnore
      boolean visible = true;

      public Site() {
        super();
      }

      public Site(String name, String displayName, String url) {
        super();
        this.name = name;
        this.displayName = displayName;
        this.url = url;
      }
      
      public Site(String name, String displayName, String url, boolean visible) {
        super();
        this.name = name;
        this.displayName = displayName;
        this.url = url;
        this.visible = visible;
      }

      public String getName() {
        return name;
      }

      public void setName(String name) {
        this.name = name;
      }

      public String getDisplayName() {
        return displayName;
      }

      public void setDisplayName(String displayName) {
        this.displayName = displayName;
      }

      public String getUrl() {
        return url;
      }

      public void setUrl(String url) {
        this.url = url;
      }

      public boolean isUserCreated() {
        return userCreated;
      }

      public void setUserCreated(boolean userCreated) {
        this.userCreated = userCreated;
      }

      public String getUid() {
        return uid;
      }

      public void setUid(String uid) {
        this.uid = uid;
      }

      public String getUserName() {
        return userName;
      }

      public void setUserName(String userName) {
        this.userName = userName;
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
        Site other = (Site) obj;
        if (name == null) {
          if (other.name != null) return false;
        } else if (!name.equals(other.name)) return false;
        return true;
      }

    }
  }

  public static class UserEmail {
    private String emailAddress;
    private boolean confirmed;
    private String confirmCode;
    private boolean isContact;
    @Embedded
    private EmailSetting setting;

    public EmailSetting getSetting() {
      return setting;
    }

    public void setSetting(EmailSetting setting) {
      this.setting = setting;
    }

    public String getEmailAddress() {
      return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
      this.emailAddress = emailAddress;
    }

    public boolean isConfirmed() {
      return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
      this.confirmed = confirmed;
    }

    public String getConfirmCode() {
      return confirmCode;
    }

    public void setConfirmCode(String confirmCode) {
      this.confirmCode = confirmCode;
    }

    public boolean isContact() {
      return isContact;
    }

    public void setContact(boolean isContact) {
      this.isContact = isContact;
    }

    public static class EmailSetting {
      private boolean exclusiveTracksAndSets;
      private boolean incomingTracks;
      private boolean message;
      private boolean activity;
      private boolean pendingGroup;
      private boolean newFollower;
      private boolean repost;
      private boolean newsLetter;
      private boolean productUpdate;
      private boolean survey;

      public EmailSetting() {
        exclusiveTracksAndSets = true;
        incomingTracks = true;
        message = true;
        activity = true;
        pendingGroup = true;
        newFollower = true;
        repost = true;
        newsLetter = true;
        productUpdate = true;
        survey = true;
      }

      public boolean isExclusiveTracksAndSets() {
        return exclusiveTracksAndSets;
      }

      public void setExclusiveTracksAndSets(boolean exclusiveTracksAndSets) {
        this.exclusiveTracksAndSets = exclusiveTracksAndSets;
      }

      public boolean isIncomingTracks() {
        return incomingTracks;
      }

      public void setIncomingTracks(boolean incomingTracks) {
        this.incomingTracks = incomingTracks;
      }

      public boolean isMessage() {
        return message;
      }

      public void setMessage(boolean message) {
        this.message = message;
      }

      public boolean isActivity() {
        return activity;
      }

      public void setActivity(boolean activity) {
        this.activity = activity;
      }

      public boolean isPendingGroup() {
        return pendingGroup;
      }

      public void setPendingGroup(boolean pendingGroup) {
        this.pendingGroup = pendingGroup;
      }

      public boolean isNewFollower() {
        return newFollower;
      }

      public void setNewFollower(boolean newFollower) {
        this.newFollower = newFollower;
      }

      public boolean isRepost() {
        return repost;
      }

      public void setRepost(boolean repost) {
        this.repost = repost;
      }

      public boolean isNewsLetter() {
        return newsLetter;
      }

      public void setNewsLetter(boolean newsLetter) {
        this.newsLetter = newsLetter;
      }

      public boolean isProductUpdate() {
        return productUpdate;
      }

      public void setProductUpdate(boolean productUpdate) {
        this.productUpdate = productUpdate;
      }

      public boolean isSurvey() {
        return survey;
      }

      public void setSurvey(boolean survey) {
        this.survey = survey;
      }
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((emailAddress == null) ? 0 : emailAddress.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      UserEmail other = (UserEmail) obj;
      if (emailAddress == null) {
        if (other.emailAddress != null) return false;
      } else if (!emailAddress.equals(other.emailAddress)) return false;
      return true;
    }
 
  
  }

  public static class UserPrefer {
    private boolean following;

    private boolean followed;

    public boolean isFollowing() {
      return following;
    }

    public void setFollowing(boolean following) {
      this.following = following;
    }

    public boolean isFollowed() {
      return followed;
    }

    public void setFollowed(boolean followed) {
      this.followed = followed;
    }

  }

  public static class UserRole {
    private Integer role;
    private Integer allowedDuration;

    public UserRole(){}
    
    public UserRole(String role)
    {
      this.role = UserRoleEnum.getTypeId(role);
      
      if (role.equals(Constant.USER_ROLE))
      {
        this.setAllowedDuration(Constant.USER_ALLOWED_DURATION);
      }
      if (role.equals(Constant.PRO_ROLE))
      {
        this.setAllowedDuration(Constant.PRO_ALLOWED_DURATION);
      }
    }
    
    public String getRole() {
      return UserRoleEnum.getTypeName(this.role);
    }

    public void setRole(String role) {
      this.role = UserRoleEnum.getTypeId(role);
    }
    
    public Integer getAllowedDuration() {
      return allowedDuration;
    }

    public void setAllowedDuration(Integer allowedDuration) {
      this.allowedDuration = allowedDuration;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((role == null) ? 0 : role.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      UserRole other = (UserRole) obj;
      if (role == null) {
        if (other.role != null) return false;
      } else if (!role.equals(other.role)) return false;
      return true;
    }
  }
  
  @Entity(noClassnameStored = true)
  public static class PlayRecord
  {
    @Id
    private ObjectId id;
    
    @Reference(lazy = true)
    @JsonIgnore
    private User user;
    
    @Reference(lazy = true)
    private Sound sound;
    
    @JsonIgnore
    public ObjectId getId() {
      return id;
    }

    public void setId(ObjectId id) {
      this.id = id;
    }

    public User getUser() {
      return user;
    }

    public void setUser(User user) {
      this.user = user;
    }

    public Sound getSound() {
      return sound;
    }

    public void setSound(Sound sound) {
      this.sound = sound;
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((profile == null) ? 0 : profile.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (!(obj instanceof User)) return false;
    User other = (User) obj;
    if (profile == null) {
      if (other.profile != null) return false;
    } else if (!profile.equals(other.getProfile())) return false;
    return true;
  }

}

package com.sound.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.NotSaved;
import com.github.jmkgreen.morphia.annotations.Reference;
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

  @JsonIgnore
  @Reference(lazy = true)
  private UserAuth auth;

  @Reference(lazy = true)
  private List<Group> groups;

  @Embedded
  private UserSocial userSocial;

  // One user one role. List for future extension.
  @Embedded
  private List<UserRole> userRoles;

  @NotSaved
  @Embedded
  private UserPrefer userPrefer;

  @Reference(lazy = true)
  private List<UserMessage> inputMessages;

  @Reference(lazy = true)
  private List<UserMessage> outputMessages;

  @JsonIgnore
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

  public List<UserEmail> getEmails() {
    return emails;
  }

  public void setEmails(List<UserEmail> emails) {
    this.emails = emails;
  }

  public void addEmail(UserEmail email) {
    this.emails = (null == this.emails) ? new ArrayList<UserEmail>() : this.emails;
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

  public List<UserMessage> getInputMessages() {
    return inputMessages;
  }

  public void setInputMessages(List<UserMessage> inputMessages) {
    this.inputMessages = inputMessages;
  }

  public void addInputMessage(UserMessage message) {
    this.inputMessages =
        (null == this.inputMessages) ? new ArrayList<UserMessage>() : this.inputMessages;
    this.inputMessages.add(message);
  }

  public void removeInputMessage(UserMessage message) {
    this.inputMessages =
        (null == this.inputMessages) ? new ArrayList<UserMessage>() : this.inputMessages;
    this.inputMessages.remove(message);
  }

  public List<UserMessage> getOutputMessages() {
    return outputMessages;
  }

  public void setOutputMessages(List<UserMessage> outputMessages) {
    this.outputMessages = outputMessages;
  }

  public void addOutputMessage(UserMessage message) {
    this.outputMessages =
        (null == this.outputMessages) ? new ArrayList<UserMessage>() : this.outputMessages;
    this.outputMessages.add(message);
  }

  public void removeOutputMessage(UserMessage message) {
    this.outputMessages =
        (null == this.outputMessages) ? new ArrayList<UserMessage>() : this.outputMessages;
    this.outputMessages.remove(message);
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
    private boolean gender;
    private boolean hasAvatar = false;
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

  }

  @Entity
  public static class UserSocial {
    private Long following;

    private Long followed;

    private Long sounds;

    private Long reposts;

    private Long soundDuration;

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

  }

  @Entity
  public static class UserExternal {
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

    public String getRole() {
      return UserRoleEnum.getTypeName(this.role);
    }

    public void setRole(String role) {
      this.role = UserRoleEnum.getTypeId(role);
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
    if (getClass() != obj.getClass()) return false;
    User other = (User) obj;
    if (profile == null) {
      if (other.profile != null) return false;
    } else if (!profile.equals(other.profile)) return false;
    return true;
  }

}

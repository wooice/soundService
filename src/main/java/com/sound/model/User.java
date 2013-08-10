package com.sound.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.Reference;
import com.sound.model.enums.GenderEnum;
import com.sound.model.enums.UserOccupationType;

@Entity(noClassnameStored= true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends BaseModel
{
	@Id 
	private ObjectId id;

	@Embedded
	private UserProfile profile;

	@Embedded
	private UserExternal external;

	@Embedded
	private List<UserEmail> emails;

	@JsonIgnore
	@Reference(lazy=true)
	private UserAuth auth;

	@Reference(lazy=true)
	private List<Group> groups;
	
	@Embedded
	private UserSocial social;

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
	

	public UserSocial getSocial() {
		return social;
	}

	public void setSocial(UserSocial social) {
		this.social = social;
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

	public void addEmail(UserEmail email)
	{
		this.emails = (null == this.emails)? new ArrayList<UserEmail>() : this.emails;
		this.emails.add(email);
	}

	public UserAuth getAuth() {
		return auth;
	}

	public void setAuth(UserAuth auth) {
		this.auth = auth;
	}
	
	public void addGroup(Group group)
	{
		this.groups = (null == this.groups)? new ArrayList<Group>():this.groups;
		
		for(Group oneGroup: this.groups)
		{
			if (oneGroup == group)
			{
				return ;
			}
		}
		this.groups.add(group);
	}
	
	public void removeGroup(Group group)
	{
		this.groups = (null == this.groups)? new ArrayList<Group>():this.groups;
		this.groups.remove(group);
	}

	@Entity
	public static class UserProfile
	{
		private String avatorUrl;
		private String alias;
		private String password;
		private String firstName;
		private String lastName;
		private String city;
		private String country;
		private String description;
		private int age;
		private boolean gender;
		private boolean hasAvatar = false;
		private List<Integer> occupations;
		
		public List<UserOccupationType> getOccupations() {
			return UserOccupationType.getTypesByIds(occupations);
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

		@JsonIgnore
		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
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
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UserProfile other = (UserProfile) obj;
			if (alias == null) {
				if (other.alias != null)
					return false;
			} else if (!alias.equals(other.alias))
				return false;
			return true;
		}
		
	}
	
	@Entity
	public static class UserSocial
	{
		private Long following;

		private Long followed;
		
		private Long sounds;
		
		private Long reposts;

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
		
	}
	
	@Entity
	public static class UserExternal
	{
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

	public static class UserEmail
	{
		private String emailAddress;

		public String getEmailAddress() {
			return emailAddress;
		}

		public void setEmailAddress(String emailAddress) {
			this.emailAddress = emailAddress;
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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (profile == null) {
			if (other.profile != null)
				return false;
		} else if (!profile.equals(other.profile))
			return false;
		return true;
	}
 
}
package com.sound.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.Reference;
import com.github.jmkgreen.morphia.annotations.Serialized;

@Entity
public class User {

	@Id private ObjectId id;

	@Embedded
	private UserProfile profile;

	@Embedded
	private UserExternal external;

	@Embedded
	private List<UserEmail> emails;

	@Reference(lazy=true)
	private UserAuth auth;

	@Reference(lazy=true)
	private List<User> following;

	@Reference(lazy=true)
	private List<User> followed;

	@Reference(lazy=true)
	private List<Group> groups;
	
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

	public List<User> getFollowing() {
		return following;
	}

	public void setFollowing(List<User> following) {
		this.following = following;
	}

	public List<User> getFollowed() {
		return followed;
	}

	public void setFollowed(List<User> followed) {
		this.followed = followed;
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

	@Entity
	public static class UserProfile
	{
		@Embedded
		private ProfileAvator avator;
		private String alias;
		private String password;
		private String firstName;
		private String lastName;
		private String location;
		private String description;
		private int age;
		private boolean gender;

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public ProfileAvator getAvator() {
			return avator;
		}

		public void setAvator(ProfileAvator avator) {
			this.avator = avator;
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

		public String getLocation() {
			return location;
		}

		public void setLocation(String location) {
			this.location = location;
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

		public boolean isGender() {
			return gender;
		}

		public void setGender(boolean gender) {
			this.gender = gender;
		}

		public class ProfileAvator
		{
			@Serialized
			private byte[] avator;

			private String extension;

			public byte[] getAvator() {
				return avator;
			}

			public void setAvator(byte[] avator) {
				this.avator = avator;
			}

			public String getExtension() {
				return extension;
			}

			public void setExtension(String extension) {
				this.extension = extension;
			}

		}
	}

	public static class UserExternal
	{
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
}
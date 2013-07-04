package com.sound.model;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.Reference;
import com.github.jmkgreen.morphia.annotations.Serialized;


public class Sound{

	@Id private ObjectId id;

	@Embedded
	private SoundProfile profile;

	@Embedded
	private SoundData soundData;

	@Reference
	private SoundSocial soundSocial;

	@Reference(lazy=true)
	private List<Sound> innerSounds;

	@Reference(lazy=true)
	private List<Sound> sets;

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

	public SoundData getSoundData() {
		return soundData;
	}

	public void setSoundData(SoundData soundData) {
		this.soundData = soundData;
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

	public static class SoundProfile
	{
		@Reference(lazy=true)
		private User owner;


		@Embedded
		private SoundPoster poster;

		private String name;
		
		/**
		 * published, private, deleted
		 */
		private int status;

		/**
		 * sound, set
		 */
		private int type;

		/**
		 * times played
		 */
		private int played;
		
		private Date createdTime;
		
		private Date modifiedTime;

		private int duration;
		
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

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public int getPlayed() {
			return played;
		}

		public void setPlayed(int played) {
			this.played = played;
		}

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

		
		public int getDuration() {
			return duration;
		}

		public void setDuration(int duration) {
			this.duration = duration;
		}

		public SoundPoster getPoster() {
			return poster;
		}

		public void setPoster(SoundPoster poster) {
			this.poster = poster;
		}

		public class SoundPoster
		{
			@Serialized
			private byte[] poster;

			private String extension;

			public byte[] getPoster() {
				return poster;
			}

			public void setPoster(byte[] poster) {
				this.poster = poster;
			}

			public String getExtension() {
				return extension;
			}

			public void setExtension(String extension) {
				this.extension = extension;
			}
		}
	}

	public static class SoundData
	{
		// back up file in gfs
		private String fileName;

		// meide route in resource server. 
		private String route;

		@Serialized
		private float[][] wave;

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getRoute() {
			return route;
		}

		public void setRoute(String route) {
			this.route = route;
		}

		public float[][] getWave() {
			return wave;
		}

		public void setWave(float[][] wave) {
			this.wave = wave;
		}
	}

}

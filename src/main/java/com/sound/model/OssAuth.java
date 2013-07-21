package com.sound.model;

public class OssAuth {
	private String accessId;
	private String accessPassword;
	private String soundBucket;
	private String imageBucket;

	public String getAccessId() {
		return accessId;
	}

	public void setAccessId(String accessId) {
		this.accessId = accessId;
	}

	public String getAccessPassword() {
		return accessPassword;
	}

	public void setAccessPassword(String accessPassword) {
		this.accessPassword = accessPassword;
	}

	public String getImageBucket() {
		return imageBucket;
	}

	public void setImageBucket(String imageBucket) {
		this.imageBucket = imageBucket;
	}

	public String getSoundBucket() {
		return soundBucket;
	}

	public void setSoundBucket(String soundBucket) {
		this.soundBucket = soundBucket;
	}

}

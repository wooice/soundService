package com.sound.dto.storage;

public class OssAuthDto {
	private String accessId;
	private String accessPassword;
	private String bucket;

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

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	@Override
	public String toString() {
		return "Oss [accessId=" + accessId + ", accessPassword="
				+ accessPassword + ", bucket=" + bucket + "]";
	}

}

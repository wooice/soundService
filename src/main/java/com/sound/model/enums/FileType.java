package com.sound.model.enums;

public enum FileType {
	SOUND("sound", "SoundBucket"), IMAGE("image", "ImageBucket");

	private String type;
	private String bucketKey;

	private FileType(String type, String bucketKey) {
		this.setType(type);
		this.setBucketKey(bucketKey);
	}

	public static FileType getFileType(String type) {
		for (FileType ft : FileType.values()) {
			if (ft.getType().equalsIgnoreCase(type)) {
				return ft;
			}
		}
		return null;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBucketKey() {
		return bucketKey;
	}

	public void setBucketKey(String bucketKey) {
		this.bucketKey = bucketKey;
	}

}

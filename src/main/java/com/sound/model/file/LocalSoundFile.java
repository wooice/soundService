package com.sound.model.file;

public class LocalSoundFile {

	private String fileName;

	/**
	 * mp3, wav...
	 */
	private String type;

	private byte[] content;

	private String ownerId;

	private float[][] waveData;

	private String status;

	/**
	 * second
	 */
	private int duration;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public float[][] getWaveData() {
		return waveData;
	}

	public void setWaveData(float[][] waveData) {
		this.waveData = waveData;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}

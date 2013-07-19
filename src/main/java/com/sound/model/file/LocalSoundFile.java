package com.sound.model.file;

public class LocalSoundFile extends LocalFile{

	private String ownerId;

	private float[][] waveData;

	private String status;

	/**
	 * second
	 */
	private int duration;

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

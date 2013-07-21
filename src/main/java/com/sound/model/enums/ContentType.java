package com.sound.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum ContentType {
	WMA("wma", "audio/x-ms-wma"), MP3("mp3", "audio/mp3"), ACP("acp",
			"audio/x-mei-aac"), AIF("aif", "audio/aiff"), BASIC("au",
			"audio/basic"), MID("mid", "audio/mid"), WAV("wav", "audio/wav");

	private final static Map<String, String> mapping;

	static {
		mapping = new HashMap<String, String>();
		for (ContentType content : ContentType.values()) {
			mapping.put(content.getMusicType(), content.getContentType());
		}
	}

	public static String getContentByMusic(String musicType) {
		return mapping.get(musicType.toLowerCase());
	}

	private String musicType;
	private String contentType;

	private ContentType(String musicType, String contentType) {
		this.musicType = musicType;
		this.contentType = contentType;
	}

	public String getMusicType() {
		return musicType;
	}

	public String getContentType() {
		return contentType;
	}

}

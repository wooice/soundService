package com.sound.dto.storage;

public class PutFileRequest extends OssRequest {
	private String contentType;

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}

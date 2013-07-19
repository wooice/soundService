package com.sound.service.impl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.model.OssAuth;


@Component
@Path("/storage")
public class StorageServiceEndpoint{
	
	@Autowired
	com.sound.service.storage.itf.RemoteStorageService remoteStorageService;
	

	@GET
	@Path("/ossauth")
	public OssAuth getOSSAuth() {
		OssAuth dto = loadOssAuthDto();
		return dto;
	}

	private OssAuth loadOssAuthDto() {
		OssAuth dto = new OssAuth();
		PropertiesConfiguration config = remoteStorageService.getRemoteStorageConfig();
		dto.setAccessId(config.getString("ACCESS_ID"));
		dto.setAccessPassword(config.getString("ACCESS_KEY"));
		dto.setBucket(config.getString("Bucket"));
		return dto;
	}

}

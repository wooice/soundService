package com.sound.service.impl;

import java.net.URL;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.model.OssAuth;
import com.sound.model.enums.FileType;

@Component
@Path("/storage")
public class StorageServiceEndpoint {

	@Autowired
	com.sound.service.storage.itf.RemoteStorageService remoteStorageService;

	@GET
	@Path("/ossauth")
	public OssAuth getOSSAuth() {
		OssAuth dto = loadOssAuthDto();
		return dto;
	}

	@GET
	@Path("/downloadurl/{type}/{file}")
	public Response getDownloadUrl(
			@NotNull @PathParam("type") String type,
			@NotNull @PathParam("file") String file
			) 
	{
		URL url = null;
		try
		{
			url = remoteStorageService.generateDownloadUrl(file,
					FileType.getFileType(type));
		}
		catch(Exception e)
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to get download url of type " + type + " and file " + file).build();
		}
		
		return Response.status(Status.OK).entity(url.toString()).build();
	}

	@GET
	@Path("/uploadurl/{type}/{file}")
	public Response getUploadUrl(
			@NotNull @PathParam("type") String type,
			@NotNull @PathParam("file") String file
			) 
	{
		URL url = null;
		try
		{
			url = remoteStorageService.generateUploadUrl(file,
					FileType.getFileType(type));
		}
		catch(Exception e)
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to get upload url of type " + type + " and file " + file).build();
		}
		return Response.status(Status.OK).entity(url.toString()).build();
	}

	private OssAuth loadOssAuthDto() {
		OssAuth dto = new OssAuth();
		PropertiesConfiguration config = remoteStorageService
				.getRemoteStorageConfig();
		dto.setAccessId(config.getString("ACCESS_ID"));
		dto.setAccessPassword(config.getString("ACCESS_KEY"));
		dto.setSoundBucket(config.getString("SoundBucket"));
		dto.setImageBucket(config.getString("ImageBucket"));
		return dto;
	}

}

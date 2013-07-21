package com.sound.service.impl;

import java.net.URL;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.dto.storage.GetFileRequest;
import com.sound.dto.storage.PutFileRequest;
import com.sound.model.OssAuth;

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
	@Path("/downloadurl/{file}")
	public Response getDownloadUrl(@PathParam("file") String file) {
		if (file == null || file.trim().equals("")) {
			return Response.status(Status.BAD_REQUEST).entity(null).build();
		}
		URL url = remoteStorageService.generateDownloadUrl(file);
		return Response.status(Status.OK).entity(url.toString()).build();
	}

	@GET
	@Path("/uploadurl/{file}")
	public Response getUploadUrl(@PathParam("file") String file) {
		if (file == null || file.trim().equals("")) {
			return Response.status(Status.BAD_REQUEST).entity(null).build();
		}
		URL url = remoteStorageService.generateUploadUrl(file);
		return Response.status(Status.OK).entity(url.toString()).build();
	}

	@GET
	@Path("/upload/{file}/{type}")
	@Produces(MediaType.APPLICATION_JSON)
	public PutFileRequest getUploadRequest(@PathParam("file") String file,
			@PathParam("type") String type) {
		return remoteStorageService.contructPutReuqest(type, file);
	}

	@GET
	@Path("/download/{file}")
	@Produces(MediaType.APPLICATION_JSON)
	public GetFileRequest getGetFileRequest(@PathParam("file") String fileName) {
		System.out.println(fileName);
		return remoteStorageService.contructGetReuqest(fileName);
	}

	private OssAuth loadOssAuthDto() {
		OssAuth dto = new OssAuth();
		PropertiesConfiguration config = remoteStorageService
				.getRemoteStorageConfig();
		dto.setAccessId(config.getString("ACCESS_ID"));
		dto.setAccessPassword(config.getString("ACCESS_KEY"));
		dto.setBucket(config.getString("Bucket"));
		return dto;
	}

}

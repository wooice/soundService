package com.sound.service.impl;

import com.sound.exception.RemoteStorageException;
import com.sound.model.enums.FileType;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Component
@Path("/upload")
public class UploadServiceEndpoint {
	private static String UPLOAD_PATH = System.getProperty("java.io.tmpdir") + "/wooice/upload/";

	@Autowired
	com.sound.service.storage.itf.RemoteStorageService remoteStorageService;

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response upload(@Context UriInfo uriInfo, @FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail){
		File uploadFile = new File(UPLOAD_PATH + fileDetail.getFileName());
		try {
			FileUtils.copyInputStreamToFile(uploadedInputStream, uploadFile);
			remoteStorageService.upload(uploadFile, FileType.getFileType(fileDetail.getType()));
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}

		return Response.status(Response.Status.OK).entity("uploaded!").build();
	}

}

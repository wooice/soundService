package com.sound.service.impl;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.exception.SoundException;
import com.sound.model.file.LocalSoundFile;
import com.sound.model.file.RemoteFile;
import com.sound.service.file.itf.FileService;
import com.sound.service.sound.itf.SoundService;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Component
@Path("/sound")
public class SoundServiceEndpoint {

	@Autowired
	FileService fileService;

	@Autowired
	SoundService soundService;

	@PUT
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response upload(
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) {
		byte[] soundData = null;
		try {
			soundData = IOUtils.toByteArray(uploadedInputStream);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("can't fetch file content.");
		}
		System.out.println(fileDetail.getName());
		LocalSoundFile sound = new LocalSoundFile();
		sound.setContent(soundData);
		sound.setType(fileDetail.getType());
		sound.setFileName(fileDetail.getName());
		try {
			sound = soundService.uniform(sound);
			RemoteFile remoteFile = fileService.upload(sound);
			soundService.save(sound, remoteFile);
		} catch (SoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	@PUT
	@Path("/save")
	public Response save(@FormParam("title") String title,
			@FormParam("description") String description) {
		return null;
	}

	@PUT
	@Path("/addToSet")
	public Response addToSet(@FormParam("userId") String userId,
			@FormParam("soundId") String soundId,
			@FormParam("SetId") String setId) {
		soundService.addToSet(soundId, setId);

		return null;
	}

	@DELETE
	@Path("/{soundId}")
	public Response delete(@PathParam("soundId") String soundId) {
		soundService.delete(soundId);

		return null;
	}

	@GET
	@Path("/{soundId}")
	public Response load(@PathParam("soundId") String soundId) {
		soundService.load(soundId);

		return null;
	}

}

package com.sound.service.itf;

import java.io.InputStream;

import javax.ws.rs.core.Response;

import com.sun.jersey.core.header.FormDataContentDisposition;

public interface SoundServicePoint {

	
	/**
	 * Input: owner id, sound name, sound data(byte[])
	 * This method do things as below:
	 * 	1. convert sound data to uniform type(mp3?)
	 * 	2. extract wave form data.
	 * 	3. save sound metadata, wave form data into mongo db.
	 * 	4. save ziped origin sound data into gridfs as back up.
	 * 	5. save uniform type sound data to resource server.
	 */
	public Response upload(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String fileName, String ownerId, String status);

	/**
	 * Input: sound id
	 * The method do things as below:
	 * 	1. mark the sound as 'deleted' in metadata
	 * 	2. delete uniform type sound data in resource server
	 */
	public Response delete(String soundId);
	


	public Response addToSet(String userId, String soundId, String ownerId);

	/**
	 * Input: sound id
	 * The method do things as below:
	 * 	1. find sound by sound id.
	 * 	2. return sound meta(may be zipped), wave data.
	 * @return
	 */
	public Response load(String soundId);

}

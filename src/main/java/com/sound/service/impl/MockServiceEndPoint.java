package com.sound.service.impl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.service.sound.impl.SoundDataService;
import com.sound.service.sound.itf.SoundService;

@Component
@Path("/mock")
public class MockServiceEndpoint {

	@Autowired
	com.sound.service.user.itf.UserService userService;

	@Autowired
	SoundService soundService;
	
	@Autowired
	SoundDataService soundDataService;

	@GET
	@Path("/create")
	public Response mock()
	{
		try
		{
			userService.deleteByAlias("robot");
			userService.createUser("robot", "robot@wooice.com", "robot123");
			
			soundService.delete("firstSound");
			soundDataService.delete("sound.mp3");
			float[][] soundWave = new float[1][1800];
			for(int i=0; i<1800;i++)
			{
				soundWave[0][i] = (float) Math.random();
			}
			soundDataService.save("sound.mp3", 256, soundWave);
			soundService.saveProfile("sound.mp3", "firstSound", "sound used to test", "robot", "private", "poster.jpg");
			
			soundService.delete("secondSound");
			soundDataService.delete("sound2.mp3");
			float[][] sound2Wave = new float[1][1800];
			for(int i=0; i<1800;i++)
			{
				sound2Wave[0][i] = (float) Math.random();
			}
			soundDataService.save("sound2.mp3", 284, sound2Wave);
			soundService.saveProfile("sound2.mp3", "secondSound", "sound used to test", "robot", "public", "poster.jpg");
		}
		catch(Exception e)
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		return Response.status(Status.OK).entity("true").build();
	}
}

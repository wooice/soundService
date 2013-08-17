package com.sound.service.endpoint;

import java.io.File;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.constant.Constant;
import com.sound.exception.RemoteStorageException;
import com.sound.exception.SoundException;
import com.sound.model.Sound.QueueNode;
import com.sound.model.enums.FileType;
import com.sound.model.file.SoundLocal;
import com.sound.service.sound.itf.SoundService;

@Component
@Path("/queue")
public class QueueProcessServiceEndpoint {

  Logger logger = Logger.getLogger(QueueProcessServiceEndpoint.class);

  @Autowired
  com.sound.service.storage.itf.RemoteStorageService remoteStorageService;

  @Autowired
  SoundService soundService;

  @POST
  @Path("/process")
  public Response process() {
    List<QueueNode> soundProcessNodes = soundService.listQueue();

    for (QueueNode node : soundProcessNodes) {
      String queueNodeFilePath = Constant.UPLOAD_QUEUE_FOLDER + File.separator + node.getFileName();

      File originFile = new File(queueNodeFilePath);

      if (!originFile.exists()) {
        continue;
      }
      SoundLocal sound = null;
      try {
        sound = soundService.processSound("robot", originFile, node.getFileName());
        remoteStorageService.upload(sound, FileType.SOUND);

        sound.setOriginName(node.getOriginFileName());
        soundService.saveData(sound, node.getOwnerAlias());
        
        soundService.dequeue(node);
      } catch (SoundException e) {
        e.printStackTrace();
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      } catch (RemoteStorageException e) {
        e.printStackTrace();
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      } catch (Exception e) {
        e.printStackTrace();
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      }
      
    }
    return Response.status(Status.OK).entity("true").build();
  }

}

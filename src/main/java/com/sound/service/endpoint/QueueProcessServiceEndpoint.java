package com.sound.service.endpoint;

import java.io.File;
import java.util.List;

import javax.annotation.security.RolesAllowed;
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
import com.sound.exception.UserException;
import com.sound.model.Sound.QueueNode;
import com.sound.model.User;
import com.sound.model.enums.FileType;
import com.sound.model.file.SoundLocal;
import com.sound.service.sound.itf.SoundService;

@Component
@Path("/queue")
@RolesAllowed(Constant.ADMIN_ROLE)
public class QueueProcessServiceEndpoint {

  Logger logger = Logger.getLogger(QueueProcessServiceEndpoint.class);

  @Autowired
  com.sound.service.storage.itf.RemoteStorageService remoteStorageService;

  @Autowired
  SoundService soundService;

  @Autowired
  com.sound.service.user.itf.UserService userService;
  
  @POST
  @Path("/process")
  public Response process() {
    List<QueueNode> soundProcessNodes = soundService.listQueue();

    for (QueueNode node : soundProcessNodes) {
      boolean success = true;
      String queueNodeFilePath = Constant.UPLOAD_QUEUE_FOLDER + File.separator + node.getFileName();

      File originFile = new File(queueNodeFilePath);

      if (!originFile.exists()) {
        continue;
      }
      SoundLocal sound = null;
      User owner = null;
      try {
        owner = node.getOwner();
        sound = soundService.processSound(owner, originFile, node.getFileName());
        remoteStorageService.upload(sound, FileType.SOUND);

        sound.setOriginName(node.getOriginFileName());
        soundService.saveData(sound, owner);
        
        soundService.dequeue(node);
      } catch (SoundException e) {
        success = false;
        e.printStackTrace();
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      } catch (RemoteStorageException e) {
        success = false;
        e.printStackTrace();
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      } catch (Exception e) {
        success = false;
        e.printStackTrace();
        return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      }
      
      try {
        if (success)
        {
            userService.sendUserMessage(null, owner, "声音上传成功", ("您的声音"+sound.getOriginName()+"上传并处理成功，将出在个人声音列表中，并推送给关注您的小伙伴。"));
        }
        else
        {
            userService.sendUserMessage(null, owner, "声音上传失败", ("非常抱歉，您的声音"+sound.getOriginName()+"上传失败，请稍后再次尝试或联系我们。"));
        }
      } catch (UserException e) {
        e.printStackTrace();
      }
      
    }
    return Response.status(Status.OK).entity("true").build();
  }

}

package com.sound.service.endpoint;

import java.io.File;
import java.net.URL;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.constant.Constant;
import com.sound.exception.SoundException;
import com.sound.exception.UserException;
import com.sound.model.Sound;
import com.sound.model.Sound.QueueNode;
import com.sound.model.User;
import com.sound.model.file.SoundLocal;
import com.sound.processor.exception.AudioProcessException;
import com.sound.service.sound.itf.SoundService;

@Component
@Path("/queueV2")
@RolesAllowed(Constant.ADMIN_ROLE)
public class QueueProcessServiceEndpointV2 {

  Logger logger = Logger.getLogger(QueueProcessServiceEndpointV2.class);

  @Autowired
  com.sound.service.storage.itf.RemoteStorageServiceV2 remoteStorageService;

  @Autowired
  SoundService soundService;

  @Autowired
  com.sound.service.user.itf.UserService userService;

  @POST
  @Path("/process")
  public Response process() {
    List<QueueNode> soundProcessNodes = soundService.listQueue();

    for (QueueNode node : soundProcessNodes) {
      String queueNodeFilePath = Constant.UPLOAD_QUEUE_FOLDER + File.separator + node.getFileName();
      SoundLocal sound = null;
      User owner = null;
      try {
        owner = node.getOwner();
        String downURL =
            remoteStorageService.getDownloadURL(node.getFileName(), "sound",
                "avthumb/wav/acodec/pcm_u8");

        File soundFile = new File(queueNodeFilePath);
        FileUtils.copyURLToFile(new URL(downURL), soundFile);
        sound = soundService.processSoundV2(owner, soundFile, node.getFileName());
        sound.setOriginName(node.getOriginFileName());
        soundService.saveData(sound, owner);

        soundService.dequeue(node);
      } catch (SoundException e) {
        Sound toDeleteSound = soundService.loadByRemoteId(node.getFileName());
        soundService.delete(toDeleteSound.getId().toString());
        
        try {
          userService.sendUserMessage(null, owner, "声音上传失败", ("非常抱歉，由于您的上传时间以达到限额，您的声音"
              + ((null == sound) ? "" : sound.getOriginName()) + "无法完成上传。"));
        } catch (UserException e1) {
          e1.printStackTrace();
        }
        return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      } catch (AudioProcessException e) {
        Sound toDeleteSound = soundService.loadByRemoteId(node.getFileName());
        soundService.delete(toDeleteSound.getId().toString());
        
        try {
          userService.sendUserMessage(null, owner, "声音上传失败", ("非常抱歉，由于您的声音文件格式不正确，您的声音"
              + ((null == sound) ? "" : sound.getOriginName()) + "无法完成上传。"));
        } catch (UserException e1) {
          e1.printStackTrace();
        }
        return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      } catch (Exception e) {
        try {
          userService.sendUserMessage(null, owner, "声音上传失败",
              ("非常抱歉，您的声音" + sound.getOriginName() + "上传失败，请稍后再次尝试或联系我们。"));
        } catch (UserException e1) {
          e1.printStackTrace();
        }
        return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }

      try {
        userService.sendUserMessage(null, owner, "声音上传成功",
            ("您的声音" + sound.getOriginName() + "上传并处理成功，将出在个人声音列表中，并推送给关注您的小伙伴。"));
      } catch (UserException e) {
        e.printStackTrace();
      }

    }
    return Response.status(Status.OK).entity("true").build();
  }

}

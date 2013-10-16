package com.sound.service.endpoint;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.constant.Constant;
import com.sound.exception.SoundException;
import com.sound.exception.UserException;
import com.sound.model.Sound;
import com.sound.model.SoundLocal;
import com.sound.model.Sound.QueueNode;
import com.sound.model.User;
import com.sound.processor.exception.AudioProcessException;
import com.sound.service.sound.itf.SoundService;

@Component
@Path("/queueV2")
@RolesAllowed(Constant.ADMIN_ROLE)
public class QueueProcessServiceEndpointV2 extends BaseEndpoint {

  Logger logger = Logger.getLogger(QueueProcessServiceEndpointV2.class);

  @Autowired
  com.sound.service.storage.itf.RemoteStorageServiceV2 remoteStorageService;

  @Autowired
  SoundService soundService;

  @Autowired
  com.sound.service.user.itf.UserService userService;

  @Context
  HttpServletRequest req;

  @POST
  @Path("/process")
  public Response process() {
    List<QueueNode> soundProcessNodes = soundService.listQueue();

    for (QueueNode node : soundProcessNodes) {
      boolean success = true;
      String titile = "";
      String message = "";

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
        sound = soundService.processSound(owner, soundFile, node.getFileName());
        sound.setOriginName(node.getOriginFileName());
        soundService.saveData(sound, owner);

        soundService.dequeue(node);
      } catch (SoundException e) {
        logger.error(e);
        Sound toDeleteSound = soundService.loadByRemoteId(node.getFileName());
        soundService.delete(toDeleteSound.getId().toString());

        success = false;
        titile = "声音上传失败";
        message =
            "非常抱歉，由于您的上传时间以达到限额，您的声音" + ((null == sound) ? "" : sound.getOriginName()) + "无法完成上传。";
      } catch (AudioProcessException e) {
        logger.error(e);
        Sound toDeleteSound = soundService.loadByRemoteId(node.getFileName());
        soundService.delete(toDeleteSound.getId().toString());

        success = false;
        titile = "声音上传失败";
        message =
            "非常抱歉，由于您的声音文件格式不正确，您的声音" + ((null == sound) ? "" : sound.getOriginName()) + "无法完成上传。";
      } catch (Exception e) {
        logger.error(e);
        success = false;
        titile = "声音上传失败";
        message = "非常抱歉，您的声音" + sound.getOriginName() + "上传失败，请稍后再次尝试或联系我们。";
      }

      if (success) {
        titile = "声音上传成功";
        message = "您的声音" + sound.getOriginName() + "上传并处理成功，将出在个人声音列表中，并推送给关注您的小伙伴。";
      }

      try {
        userService.sendUserMessage(null, owner, titile, message);

        HttpSession session = req.getSession(false);
        if (null != session.getAttribute("eventOutput")) {
          EventOutput eventOutput = (EventOutput) session.getAttribute("eventOutput");
          final OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
          eventBuilder.name("newMessage");
          OutboundEvent event = eventBuilder.build();
          eventOutput.write(event);
        }

      } catch (UserException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
    return Response.status(Status.OK).build();
  }

}

package com.sound.service.endpoint;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sound.constant.Constant;
import com.sound.exception.SoundAuthException;
import com.sound.exception.SoundException;
import com.sound.filter.authentication.ResourceAllowed;
import com.sound.model.Sound.QueueNode;
import com.sound.model.SoundLocal;
import com.sound.model.User;
import com.sound.processor.exception.AudioProcessException;
import com.sound.service.sound.itf.QueueService;
import com.sound.service.sound.itf.SoundService;

@Component
@Path("/queue")
@ResourceAllowed
public class QueueProcessServiceEndpoint extends BaseEndpoint {

  Logger logger = Logger.getLogger(QueueProcessServiceEndpoint.class);

  @Autowired
  com.sound.service.storage.itf.RemoteStorageService remoteStorageService;
  
  @Autowired
  com.sound.service.user.itf.MessageService messageService;

  @Autowired
  SoundService soundService;
  
  @Autowired
  QueueService queueService;

  @Autowired
  com.sound.service.user.itf.UserService userService;

  @Context
  HttpServletRequest req;

  @POST
  @Path("/process")
  public Response process() {
    List<QueueNode> soundProcessNodes = queueService.listQueue();
    
    for (QueueNode node : soundProcessNodes){
      queueService.dequeue(node);
    }

    for (QueueNode node : soundProcessNodes) {
      boolean success = true;
      String titile = "";
      String message = "";

      SoundLocal sound = null;
      User owner = node.getOwner();
      try {
        String downURL =
            remoteStorageService.getDownloadURL(node.getFileName(), "sound",
                "avthumb/wav/acodec/pcm_s16le");
        sound =
            queueService.processSound(owner, downURL, node);
        sound.setOriginName(node.getOriginFileName());

        soundService.promoteUser(owner);
      } 
      catch (SoundAuthException authException)
      {
        success = false;
        titile = "声音上传失败";
        message = "  非常抱歉，您可能不是声音" + node.getOriginFileName() + "的原作者(表演者或词曲作者)，\n";
        
        if (null != authException.getAlbum())
        {
          message += "      唱片集:    "+ authException.getAlbum() + "\n";
        }
        if (null != authException.getAlbumArtists())
        {
          message += "      唱片集艺术家:  " + authException.getAlbumArtists() + "\n";
        }
        if (null != authException.getArtists())
        {
          message += "      歌曲艺术家:   " + authException.getArtists() + "\n";
        }
        if (null != authException.getComposer())
        {
          message += "      歌曲曲作者:      " + authException.getComposer() + "\n";
        }
        
        message += "  由于以上原因，您的声音 无法完成上传。请确认您是声音的原作者，并修正上传的音频有完善的版权信息， 以免侵犯他人的著作权。";
        soundService.deleteByRemoteId(node.getFileName());
      }
      catch (SoundException e) {
        success = false;
        titile = "声音上传失败";
        // if stream or format info not ready, just ignore it.
        if ("EMPTY_STREAM".equals(e.getMessage()) || "NO_FORMATINFO".equals(e.getMessage())) {
          continue;
        }
        if ("TOTAL_LIMIT_ERROR".equals(e.getMessage())) {
          message =
              "非常抱歉，由于您已达到上传时间限额(" + (owner.getUserRoles().get(0).getAllowedDuration())
                  + "分钟)，您的声音" + ((null == sound) ? "" : node.getOriginFileName())
                  + "无法完成上传。请上传更多原创声音获得高级用户权限。";
          soundService.deleteByRemoteId(node.getFileName());
        }
        if ("WEEKLY_LIMIT_ERROR".equals(e.getMessage())) {
          message =
              "非常抱歉，由于您已达到本周上传限额(" + Constant.WEEKLY_ALLOWED_DURATION + "分钟)，您的声音"
                  + ((null == sound) ? "" : node.getOriginFileName()) + "无法完成上传。";
          soundService.deleteByRemoteId(node.getFileName());
        }
      } 
      catch (AudioProcessException e) {
        success = false;
        
        soundService.deleteByRemoteId(node.getFileName());
        titile = "声音上传失败";
        message =
            "非常抱歉，由于您的声音文件格式不正确，您的声音" + ((null == sound) ? "" : node.getOriginFileName()) + "无法完成上传。";
      } catch (Exception e) {
        success = false;
        titile = "声音上传失败";
        message = "非常抱歉，您的声音" + node.getOriginFileName() + "上传失败，请稍后再次尝试或联系我们。";
      }

      if (success) {
        titile = "声音上传成功";
        message = "您的声音" + node.getOriginFileName() + "上传并处理成功，将出在个人声音列表中，并推送给关注您的小伙伴。";
      }

      messageService.sendUserMessage(null, owner, titile, message);
    }
    return Response.status(Status.OK).build();
  }

}

package com.sound.service.user.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sound.dao.UserDAO;
import com.sound.dao.UserMessageDAO;
import com.sound.exception.UserException;
import com.sound.model.User;
import com.sound.model.UserMessage;
import com.sound.service.user.itf.UserService;

@Service
@Scope("singleton")
public class MessageService implements com.sound.service.user.itf.MessageService {

  Logger logger = Logger.getLogger(MessageService.class);
  
  @Autowired
  UserDAO userDAO;
  
  @Autowired
  UserMessageDAO userMessageDAO;
  
  @Autowired
  UserService userService;
  
  @Override
  public void sendUserMessage(User fromUser, User toUser, String topic, String content) {
    UserMessage message = new UserMessage();
    message.setFrom(fromUser);
    message.setTo(toUser);
    message.setTopic(topic);
    message.setContent(content);
    message.setDate(new Date());
    message.setUpdatedDate(new Date());
    message.setFromStatus("read");
    message.setToStatus("unread");
    
    userMessageDAO.save(message);
  }
  
  @Override
  public UserMessage replyMessage(UserMessage subMessage, User fromUser, User toUser, String topic, String content)
  {
    UserMessage message = new UserMessage();
    message.setFrom(fromUser);
    message.setTo(toUser);
    message.setTopic(topic);
    message.setContent(content);
    message.setDate(new Date());
    message.setUpdatedDate(new Date());
    message.setFromStatus("read");
    message.setToStatus("unread");

    subMessage.setToStatus("unread");
    subMessage.getReplies().add(message);
    subMessage.setUpdatedDate(new Date());
    userMessageDAO.save(subMessage);
 
    return message;
  }

  @Override
  public List<UserMessage> getUserMessages(User curUser, Integer pageNum, Integer perPage) {
    List<UserMessage> allMessages = userMessageDAO.findMessageList(curUser, pageNum-1, pageNum*perPage , "-updatedDate");
    List<UserMessage> messages = new ArrayList<UserMessage>();
    
    for (UserMessage message: allMessages)
    {
        UserMessage userMessage = sanitizeMessage(curUser, message);     
        if (null != userMessage)
        {
          messages.add(userMessage);
        }
    }
    
    return messages;
  }
  

  @Override
  public UserMessage getUserMessage(User curUser, String messageId) {
    UserMessage userMessage = userMessageDAO.findOne("_id", new ObjectId(messageId));
    
    return sanitizeMessage(curUser, userMessage);
  }

  @Override
  public long countUserMessage(User curUser) {
    return userMessageDAO.countMessageList(curUser);
  }

  @Override
  public long countUnreadMessage(User user) {
    return  userMessageDAO.countUnreadMessage(user);
  }

  @Override
  public void markUserMessage(User curUser, String messageId, String status) throws UserException {
    UserMessage userMessage = userMessageDAO.findOne("_id", new ObjectId(messageId));

    if (null!=userMessage.getFrom() && userMessage.getFrom().equals(curUser) && "deleted".equals(status))
    {
       userMessageDAO.updateProperty("_id", userMessage.getId(), "fromStatus", status);
    }
    
    if (null!=userMessage.getTo() && userMessage.getTo().equals(curUser) && ("deleted".equals(status) || "read".equals(status)))
    {
       userMessageDAO.updateProperty("_id", userMessage.getId(), "toStatus", status);
    }
    
    if("read".equals(status))
    {
      if (userMessage.getReplies().size() > 0)
      {
        for (UserMessage reply: userMessage.getReplies())
        {
          if (reply.getTo().equals(curUser))
          {
            reply.setToStatus("read");
          }
        }
      
        userMessageDAO.save(userMessage);
      }
    }
  }

  private UserMessage sanitizeMessage(User curUser, UserMessage userMessage)
  {
    if (null == userMessage.getFrom() && null == userMessage.getTo())
    {
      return null;
    }
    
    if (curUser.equals(userMessage.getFrom()) && userMessage.getFromStatus().equals("deleted"))
    {
      return null;
    }
    
    if (userMessage.getTo().equals(curUser) && userMessage.getToStatus().equals("deleted"))
    {
      return null;
    }
    
    List<UserMessage> replies = new ArrayList<UserMessage>();
    for (UserMessage reply: userMessage.getReplies())
    {
      if (curUser.equals(reply.getFrom()) && reply.getFromStatus().equals("deleted"))
      {
        continue;
      }
      if (reply.getTo().equals(curUser) && reply.getToStatus().equals("deleted"))
      {
        continue;
      }
      replies.add(reply);
    }
    
    userMessage.setReplies(replies);
    
    return userMessage;
  }

}

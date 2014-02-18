package com.sound.service.user.itf;

import java.util.List;

import com.sound.exception.UserException;
import com.sound.model.User;
import com.sound.model.UserMessage;

public interface MessageService {
  public void sendUserMessage(User fromUser, User toUser, String topic, String content);
  
  public UserMessage replyMessage(UserMessage message, User fromUser, User toUser, String topic, String content);

  public void markUserMessage(User curUser, String messageId, String status) throws UserException;
  
  public UserMessage getUserMessage(User curUser, String messageId);
  
  public long countUserMessage(User curUser);
  
  public List<UserMessage> getUserMessages(User curUser, Integer pageNum, Integer perPage);
  
  public long countUnreadMessage(User user);
}

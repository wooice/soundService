package com.sound.service.user.itf;

import java.util.List;

import com.sound.exception.SoundException;
import com.sound.exception.UserException;
import com.sound.model.User;

public interface UserSocialService {
  public Long follow(User fromUser, User toUser) throws UserException;

  public Long unfollow(User fromUser, User toUser) throws UserException;

  public void createGroup(User user, String groupName, String description) throws UserException;

  public void dismissGroup(User user, String groupName) throws UserException;

  public void joinGroup(User user, String groupName) throws UserException;

  public void leaveGroup(User user, String groupName) throws UserException;

  public void promoteGroupAdmin(User owner, User admin, String groupName) throws UserException;

  public void demoteGroupAdmin(User owner, User admin, String groupName) throws UserException;

  public List<User> getFollowedUsers(User user, Integer pageNum, Integer pageSize)
      throws UserException;

  public List<User> getFollowingUsers(User user, Integer pageNum, Integer pageSize)
      throws UserException;

  public List<User> recommandUsersForUser(User user, Integer pageNum, Integer pageSize)
      throws UserException, SoundException;

  public List<User> recommandUsersByTags(User currentUser, List<String> tagLabels, Integer pageNum, Integer pageSize)
      throws UserException, SoundException;
}

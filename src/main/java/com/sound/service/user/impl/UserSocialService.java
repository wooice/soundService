package com.sound.service.user.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sound.dao.GroupDAO;
import com.sound.dao.SoundDAO;
import com.sound.dao.UserConnectDAO;
import com.sound.dao.UserDAO;
import com.sound.exception.SoundException;
import com.sound.exception.UserException;
import com.sound.model.Group;
import com.sound.model.Sound;
import com.sound.model.Tag;
import com.sound.model.User;
import com.sound.model.UserActivity.UserConnect;
import com.sound.service.sound.itf.SoundSocialService;
import com.sound.service.sound.itf.TagService;
import com.sound.util.SocialUtils;

@Service
@Scope("singleton")
public class UserSocialService implements com.sound.service.user.itf.UserSocialService {

  @Autowired
  UserDAO userDAO;

  @Autowired
  UserConnectDAO userConnectDAO;

  @Autowired
  SoundDAO soundDAO;

  @Autowired
  GroupDAO groupDAO;

  @Autowired
  TagService tagService;

  @Autowired
  SoundSocialService soundSocialService;

  @Override
  public Long follow(User fromUser, User toUser) throws UserException {
    if (null == fromUser) {
      throw new UserException("From user not found.");
    }

    if (null == toUser) {
      throw new UserException("To user not found.");
    }

    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("fromUser", fromUser);
    cratiaries.put("toUser", toUser);
    UserConnect userConnected = userConnectDAO.findOne(cratiaries);

    if (userConnected != null) {
      return toUser.getUserSocial().getFollowed();
    }

    UserConnect userConnect = new UserConnect();
    userConnect.setFromUser(fromUser);
    userConnect.setToUser(toUser);
    userConnect.setCreatedTime(new Date());

    userConnectDAO.save(userConnect);

    userDAO.increase("profile.alias", fromUser.getProfile().getAlias(), "userSocial.following");
    userDAO.increase("profile.alias", toUser.getProfile().getAlias(), "userSocial.followed");

    return toUser.getUserSocial().getFollowed() + 1;
  }

  @Override
  public Long unfollow(User fromUser, User toUser) throws UserException {
    if (null == fromUser) {
      throw new UserException("From user not found.");
    }

    if (null == toUser) {
      throw new UserException("To user not found.");
    }

    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("fromUser", fromUser);
    cratiaries.put("toUser", toUser);
    UserConnect userConnected = userConnectDAO.findOne(cratiaries);

    if (userConnected == null) {
      return toUser.getUserSocial().getFollowed();
    }

    userConnectDAO.delete(userConnected);

    userDAO.decrease("profile.alias", fromUser.getProfile().getAlias(), "userSocial.following");
    userDAO.decrease("profile.alias", toUser.getProfile().getAlias(), "userSocial.followed");

    return toUser.getUserSocial().getFollowed() - 1;
  }

  @Override
  public void createGroup(User user, String groupName, String description) throws UserException {

    if (null == user) {
      throw new UserException("User not found.");
    }
    Group group = new Group();
    group.setName(groupName);
    group.setDescription(description);
    group.setOwner(user);
    groupDAO.save(group);

    user.addGroup(group);
    userDAO.updateProperty("profile.alias", user.getProfile().getAlias(), "groups",
        user.getGroups());
  }

  @Override
  public void dismissGroup(User user, String groupName) throws UserException {

    if (null == user) {
      throw new UserException("User not found.");
    }

    Group group = groupDAO.findOne("name", groupName);
    if (group.getOwner().equals(user)) {
      List<User> users = userDAO.find("group.name", groupName);
      for (User groupUser : users) {
        groupUser.removeGroup(group);
        userDAO.updateProperty("profile.alias", groupUser.getProfile().getAlias(), "groups",
            groupUser.getGroups());
      }

      groupDAO.delete(group);
    } else {
      throw new UserException("User " + user.getProfile().getAlias()
          + " don't have rights to delete the group " + groupName);
    }
  }

  @Override
  public void joinGroup(User user, String groupName) throws UserException {

    if (null == user) {
      throw new UserException("User not found.");
    }

    Group group = groupDAO.findOne("name", groupName);
    user.addGroup(group);
    userDAO.updateProperty("profile.alias", user.getProfile().getAlias(), "groups",
        user.getGroups());
  }

  @Override
  public void leaveGroup(User user, String groupName) throws UserException {
    if (null == user) {
      throw new UserException("User not found.");
    }

    Group group = groupDAO.findOne("name", groupName);
    user.removeGroup(group);
    userDAO.updateProperty("profile.alias", user.getProfile().getAlias(), "groups",
        user.getGroups());
  }

  @Override
  public void promoteGroupAdmin(User owner, User admin, String groupName) throws UserException {
    if (null == owner) {
      throw new UserException("Owner not found.");
    }

    if (null == admin) {
      throw new UserException("Admin  not found.");
    }

    Group group = groupDAO.findOne("name", groupName);
    if (group.getOwner().equals(admin)) {
      group.addAdmin(admin);
      groupDAO.updateProperty("name", groupName, "admins", group.getAdmins());
    } else {
      throw new UserException("User " + owner.getProfile().getAlias()
          + " don't have rights to promote admin of the group " + groupName);
    }
  }

  @Override
  public void demoteGroupAdmin(User owner, User admin, String groupName) throws UserException {
    if (null == owner) {
      throw new UserException("Owner not found.");
    }

    if (null == admin) {
      throw new UserException("Admin not found.");
    }

    Group group = groupDAO.findOne("name", groupName);
    if (group.getOwner().equals(admin)) {
      group.removeAdmin(admin);
      groupDAO.updateProperty("name", groupName, "admins", group.getAdmins());
    } else {
      throw new UserException("User " + owner.getProfile().getAlias()
          + " don't have rights to promote admin of the group " + groupName);
    }
  }

  @Override
  public List<User> getFollowedUsers(User toUser, Integer pageNum, Integer pageSize)
      throws UserException {
    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("toUser", toUser);
    List<UserConnect> list = userConnectDAO.findWithRange(cratiaries, pageNum-1, pageNum*pageSize, "createdTime");
    List<User> results = new ArrayList<User>();
    for (UserConnect uc : list) {
      results.add(uc.getFromUser());
    }
    return results;
  }

  @Override
  public List<User> getFollowingUsers(User fromUser, Integer pageNum, Integer pageSize)
      throws UserException {
    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("fromUser", fromUser);
    List<UserConnect> list = userConnectDAO.findWithRange(cratiaries, pageNum-1, pageNum*pageSize, "createdTime");
    List<User> results = new ArrayList<User>();
    for (UserConnect uc : list) {
      results.add(uc.getToUser());
    }
    return results;
  }

  private List<User> getAllFollowingUsers(User fromUser) throws UserException {
    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("fromUser", fromUser);
    List<UserConnect> list = userConnectDAO.findWithRange(cratiaries, 0, 25, "-createdTime");
    List<User> result = new ArrayList<User>();
    for (UserConnect uc : list) {
      result.add(uc.getToUser());
    }
    return result;
  }

  private List<User> recommandUsersBySocial(User user) throws UserException, SoundException {
    List<User> followingUsers = this.getAllFollowingUsers(user);

    if (followingUsers.size() == 0) {
      return new ArrayList<User>();
    }

    Map<User, Long> potentialFollowing = new HashMap<User, Long>();

    // find 1-class potential follow targets
    for (User oneUser : followingUsers) {
      List<User> firstClass = this.getAllFollowingUsers(oneUser);
      firstClass.removeAll(followingUsers);
      for (User firstUser : firstClass) {
        if (potentialFollowing.containsKey(firstUser)) {
          potentialFollowing.put(firstUser, potentialFollowing.get(firstUser) + 1);
        } else {
          potentialFollowing.put(firstUser, 1l);
        }
      }
    }

    // add weight to impress 1-class
    for (User oneUser : potentialFollowing.keySet()) {
      potentialFollowing.put(oneUser, potentialFollowing.get(oneUser)
          + SocialUtils.FIRST_CLASS_WEIGHT);
    }

    // find 2-class potential follow targets
    for (User oneUser : potentialFollowing.keySet()) {
      List<User> secondClass = this.getAllFollowingUsers(oneUser);
      secondClass.removeAll(followingUsers);
      secondClass.removeAll(potentialFollowing.keySet());
      for (User secondUser : secondClass) {
        if (potentialFollowing.containsKey(secondUser)) {
          potentialFollowing.put(secondUser, 1l);
        } else {
          potentialFollowing.put(secondUser, potentialFollowing.get(secondUser) + 1);
        }
      }
    }

    List<User> allResult =
        SocialUtils.toSeqList(SocialUtils.sortMapByValue(potentialFollowing, false));

    return allResult;
  }

  private List<User> recommandUsersByTags(User curUser, Set<Tag> tags) throws UserException, SoundException {
    Map<User, Long> userTagNumMap = new HashMap<User, Long>();

    List<Tag> tagList = new ArrayList<Tag>();
    tagList.addAll(tags);
    List<Sound> sounds = soundDAO.findByTag(curUser, tagList, 0, 25);

    for (Sound sound : sounds) {
      User user = sound.getProfile().getOwner();
      
      if (curUser.equals(user))
      {
        continue;
      }
      if (userTagNumMap.containsKey(user)) {
        userTagNumMap.put(user, userTagNumMap.get(user) + 1);
      } else {
        userTagNumMap.put(user, (long) 1);
      }
    }

    List<User> sortedUsers = SocialUtils.toSeqList(SocialUtils.sortMapByValue(userTagNumMap, false));

    return sortedUsers;
  }

  @Override
  public List<User> recommandUsersForUser(User user, Integer pageNum, Integer pageSize)
      throws UserException, SoundException {
    List<User> bySocial = recommandUsersBySocial(user);

    List<Sound> liked = soundDAO.getRecommendSoundsByUser(user, 0, 25);
    Set<Tag> tags = new HashSet<Tag>();
    for (Sound sound : liked) {
      tags.addAll(sound.getTags());
    }
    List<User> byTags = recommandUsersByTags(user, tags);
    List<User> candidates = combineSocialAndTagsRecommandation(bySocial, byTags);
    List<User> results = new ArrayList<User>();

    for (User oneUser : candidates) {
      if (!user.equals(oneUser))
      {
        Map<String, Object> cretiaria = new HashMap<String, Object>();
        cretiaria.put("fromUser", user);
        cretiaria.put("toUser", oneUser);
        UserConnect uc = userConnectDAO.findOne(cretiaria);
  
        if (uc == null) {
          results.add(oneUser);
        }
      }
    }

    List<User> toReturn = SocialUtils.subList(results, pageNum, pageSize);
    List<String> exclusives = new ArrayList<String>();
    for (User temp: toReturn)
    {
      exclusives.add(temp.getProfile().getAlias());
    }

    if (toReturn.size() < pageSize) {
      toReturn.addAll(recommandRandomUsers(user, exclusives, pageSize - toReturn.size()));
    }

    return toReturn;
  }

  private List<User> recommandRandomUsers(User currentUser, List<String> exclusiveUsers, int number) {
    Map<String, List<Object>> exclude = new HashMap<String, List<Object>>();
    @SuppressWarnings("unchecked")
    List<Object> alias = (List<Object>) ((null == exclusiveUsers)?new ArrayList<Object>(): exclusiveUsers);
    alias.add(currentUser.getProfile().getAlias());
    exclude.put("profile.alias", alias);
    List<User> topUsers = userDAO.findTopOnes(number, exclude, "-userSocial.sounds, userSocial.followed,-profile.createDate");
    List<User> results = new ArrayList<User>();

    for (User user : topUsers) {
      Map<String, Object> cretiaria = new HashMap<String, Object>();
      cretiaria.put("fromUser", currentUser);
      cretiaria.put("toUser", user);
      UserConnect uc = userConnectDAO.findOne(cretiaria);

      if (uc == null) {
        results.add(user);
      }
    }

    return results;
  }

  private List<User> combineSocialAndTagsRecommandation(List<User> bySocial, List<User> byTags) {
    List<User> result = new ArrayList<User>();
    for (User user : bySocial) {
      if (byTags.contains(user)) {
        result.add(user);
      }
    }
    bySocial.removeAll(result);
    byTags.removeAll(result);
    int i = 0;
    while (i < bySocial.size() && i < byTags.size()) {
      result.add(bySocial.get(i));
      result.add(byTags.get(i));
      i++;
    }
    for (int j = i; j < bySocial.size(); j++) {
      result.add(bySocial.get(j));
    }
    for (int j = i; j < byTags.size(); j++) {
      result.add(byTags.get(j));
    }

    return result;
  }

  @Override
  public List<User> recommandUsersByTags(User currentUser, List<String> tagLabels, Integer pageNum,
      Integer pageSize) throws UserException, SoundException 
  {
    Set<Tag> tags = new HashSet<Tag>();

    for (String label : tagLabels) {
      Tag tag = new Tag();
      tag.setLabel(label);
      tags.add(tagService.get(tag, false));
    }

    List<User> byTags = recommandUsersByTags(currentUser, tags);

    List<User> toReturn = SocialUtils.subList(byTags, pageNum, pageSize);
    List<String> exclusives = new ArrayList<String>();
    for (User temp: toReturn)
    {
      exclusives.add(temp.getProfile().getAlias());
    }

    if (toReturn.size() < pageSize) {
      toReturn.addAll(recommandRandomUsers(currentUser, exclusives, pageSize - toReturn.size()));
    }

    return toReturn;
  }
}

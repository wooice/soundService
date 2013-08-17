package com.sound.service.user.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sound.dao.UserAuthDAO;
import com.sound.dao.UserConnectDAO;
import com.sound.dao.UserDAO;
import com.sound.dto.UserBasicProfileDTO;
import com.sound.dto.UserSnsProfileDTO;
import com.sound.exception.UserException;
import com.sound.model.User;
import com.sound.model.User.UserEmail;
import com.sound.model.User.UserExternal;
import com.sound.model.User.UserPrefer;
import com.sound.model.User.UserProfile;
import com.sound.model.UserActivity.UserConnect;
import com.sound.model.UserAuth;
import com.sound.model.UserAuth.ChangeHistory;
import com.sound.model.enums.FileType;
import com.sound.service.storage.itf.RemoteStorageService;

@Service
@Scope("singleton")
public class UserService implements com.sound.service.user.itf.UserService {
  
  Logger logger = Logger.getLogger(UserService.class);

  @Autowired
  UserDAO userDAO;

  @Autowired
  UserConnectDAO userConnectDAO;

  @Autowired
  RemoteStorageService remoteStorageService;
  
  @Autowired
  UserAuthDAO userAuthDAO;

  @Override
  public User getUserByAlias(String userAlias) {
    User user = userDAO.findOne("profile.alias", userAlias);
    
    if (user == null)
      return null;

    if (user.getProfile().hasAvatar()) {
      user.getProfile().setAvatorUrl(
          remoteStorageService.generateDownloadUrl(user.getProfile().getAlias(),
              FileType.getFileType("image")).toString());
    }

    user.setUserPrefer(getUserPreferOfSound(user, user));

    return user;
  }

  @Override
  public User getUserByEmail(String emailAddress) {
    User user = userDAO.findOne("emails.emailAddress", emailAddress);

    return user;
  }

  @Override
  public User createUser(String userAlias, String emailAddress, String password)
      throws UserException {
    User user = this.getUserByAlias(userAlias);

    if (null != user) {
      throw new UserException("User with alias " + userAlias + " exists.");
    }

    user = this.getUserByEmail(emailAddress);

    if (null != user) {
      throw new UserException("User with email address " + emailAddress + " exists.");
    }

    user = new User();
    User.UserProfile profile = new User.UserProfile();
    profile.setAlias(userAlias);
    profile.setPassword(password);
    user.setProfile(profile);
    User.UserEmail email = new User.UserEmail();
    email.setEmailAddress(emailAddress);
    email.setConfirmCode(generateConfirmationCode());
    email.setConfirmed(false);
    user.addEmail(email);
    User.UserSocial social = new User.UserSocial();
    social.setFollowed(0L);
    social.setFollowing(0L);
    social.setSounds(0L);
    user.setSocial(social);

    userDAO.save(user);

    return user;
  }

  @Override
  public User updatePassword(String emailAddress, String password, String ip)
          throws UserException {
      User user = this.getUserByEmail(emailAddress);

      if (null == user) {
          throw new UserException("Cannot find user with email address : "
                  + emailAddress);
      }

      UserAuth auth = user.getAuth();
      if (auth == null) {
          auth = new UserAuth();
          user.setAuth(auth);
          auth.setHistories(new ArrayList<ChangeHistory>());
      }
      auth.setId(user.getId());
      auth.setPassword(password);
      ChangeHistory history = new ChangeHistory();
      history.setIp(ip);
      history.setModifiedDate(new Date());
      history.setPassword(password);
      auth.getHistories().add(history);

      userAuthDAO.save(auth);

      return user;
  }

  public void deleteByAlias(String userAlias) {
    userDAO.deleteByProperty("profile.alias", userAlias);
  }

  private UserPrefer getUserPreferOfSound(User currentUser, User targetUser) {
    UserPrefer userPrefer = new UserPrefer();

    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("fromUser", currentUser);
    cratiaries.put("toUser", targetUser);
    UserConnect following = userConnectDAO.findOne(cratiaries);

    if (null != following) {
      userPrefer.setFollowing(true);
    }

    cratiaries.clear();
    cratiaries.put("fromUser", targetUser);
    cratiaries.put("toUser", currentUser);
    UserConnect followed = userConnectDAO.findOne(cratiaries);

    if (null != followed) {
      userPrefer.setFollowed(true);
    }

    return userPrefer;
  }
  
  @Override
  public User updateUserBasicProfile(String userAlias,
          UserBasicProfileDTO profileDTO) throws UserException {
      User user = this.getUserByAlias(userAlias);

      if (null == user) {
          throw new UserException("Cannot find user : " + userAlias);
      }

      UserProfile profile = user.getProfile();

      if (StringUtils.isNotBlank(profileDTO.getAlias())) {
          profile.setAlias(profileDTO.getAlias());
      }

      if (StringUtils.isNotBlank(profileDTO.getAvatorUrl())) {
          profile.setAvatorUrl(profileDTO.getAvatorUrl());
          profile.setHasAvatar(true);
      }

      if (StringUtils.isNotBlank(profileDTO.getFirstname())) {
          profile.setFirstName(profileDTO.getFirstname());
      }

      if (StringUtils.isNotBlank(profileDTO.getLastname())) {
          profile.setLastName(profileDTO.getLastname());
      }

      if (StringUtils.isNotBlank(profileDTO.getCity())) {
          profile.setCity(profileDTO.getCity());
      }

      if (StringUtils.isNotBlank(profileDTO.getCountry())) {
          profile.setCountry(profileDTO.getCountry());
      }

      if (StringUtils.isNotBlank(profileDTO.getDescription())) {
          profile.setDescription(profileDTO.getDescription());
      }

      if (CollectionUtils.isNotEmpty(profileDTO.getOccupations())) {
          profile.setOccupations(profileDTO.getOccupations());
      }

      userDAO.updateProperty("_id", user.getId(), "profile", profile);

      return user;

  }

  @Override
  public User updateUserSnsProfile(String userAlias, UserSnsProfileDTO snsDTO)
          throws UserException {
      User user = this.getUserByAlias(userAlias);

      if (null == user) {
          throw new UserException("Cannot find user : " + userAlias);
      }

      UserExternal external = user.getExternal();

      if (StringUtils.isNotBlank(snsDTO.getWebsite())) {
          external.setWebsite(snsDTO.getWebsite());
      }

      if (StringUtils.isNotBlank(snsDTO.getSina())) {
          external.setSina(snsDTO.getSina());
      }

      if (StringUtils.isNotBlank(snsDTO.getQq())) {
          external.setQq(snsDTO.getQq());
      }

      if (StringUtils.isNotBlank(snsDTO.getRenren())) {
          external.setRenren(snsDTO.getRenren());
      }

      if (StringUtils.isNotBlank(snsDTO.getDouban())) {
          external.setDouban(snsDTO.getDouban());
      }

      userDAO.updateProperty("_id", user.getId(), "external", external);

      return user;
  }
  
  @Override
  public User addEmailAddress(String userAlias, String emailAddress) throws UserException {
    User user = this.getUserByAlias(userAlias);
    if (user == null) {
        throw new UserException("Cannot find user : " + userAlias);
    }
    
    List<UserEmail> emails = user.getEmails();
    UserEmail email = new UserEmail();
    email.setConfirmed(false);
    email.setEmailAddress(emailAddress);
    email.setConfirmCode(generateConfirmationCode());
    emails.add(email);
    userDAO.updateProperty("_id", user.getId(), "emails", emails);
    
    return user;
  }

  @Override
  public void sendEmailAddressConfirmation(String userAlias, String emailAddress)
      throws UserException {
    User user = this.getUserByAlias(userAlias);
    if (user == null) {
        throw new UserException("Cannot find user : " + userAlias);
    }
    List<UserEmail> emails = user.getEmails();
    for (UserEmail email : emails) {
      if (email.getEmailAddress().equals(emailAddress)) {
        doSendEmail(emailAddress, userAlias, email.getConfirmCode());
      }
    }
  }

  @Override
  public void confirmEmailAddress(String confirmCode) throws UserException {
    User user = userDAO.findOne("emails.confirmCode", confirmCode);
    if (user == null) {
      throw new UserException("Cannot find user by email confirm code: " + confirmCode);
      
    }
    List<UserEmail> emails = user.getEmails();
    for (UserEmail email : emails) {
      if (email.getConfirmCode().equals(confirmCode)) {
        email.setConfirmed(true);
      }
    }
    
    userDAO.updateProperty("_id", user.getId(), "emails", emails);
  }
  
  private static String generateConfirmationCode() {
    return RandomStringUtils.random(32, true, true);
  }
  
  private static void doSendEmail(String emailAddress, String userAlias, String confirmCode) {
    try {
      HtmlEmail email = new HtmlEmail();
      email.setAuthentication("wooice", "dxd123456");
      email.setCharset("utf-8");
      email.setSubject("Wooice Email Address Confirmation");
      email.setHostName("smtp.126.com");
      email.setFrom("wooice@126.com", "Wooice");
      email.addTo(emailAddress, userAlias);
      email.setContent(generateHtmlEmailBody(confirmCode, userAlias), "text/html; charset=UTF-8");
      email.send();
    } catch (EmailException e) {
      e.printStackTrace();
    }
  }
  
  private static String generateHtmlEmailBody(String code, String userAlias) {
    StringBuilder sb = new StringBuilder();
    sb.append("<h2>Welcome to Wooice Family</h2>");
    sb.append("Hi " + userAlias +",<br/><br/>");
    sb.append("We've received a request to add this email address to a Wooice account. Please click ");
    sb.append("<a href=\"http://localhost:8080/commonService/confirmEmail/"+ code + "\">this link to confirm that it's OK.</a> <br/><br/>");
    sb.append("<h4>The WOOICE Team<h4/>");
    return sb.toString();
  }
  
  public static void main(String[] args) {
    doSendEmail("dugu108@qq.com", "TEST", "");
  }
  
}

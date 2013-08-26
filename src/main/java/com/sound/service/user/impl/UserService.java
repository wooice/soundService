package com.sound.service.user.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sound.constant.Constant;
import com.sound.dao.UserAuthDAO;
import com.sound.dao.UserConnectDAO;
import com.sound.dao.UserDAO;
import com.sound.dao.UserMessageDAO;
import com.sound.exception.UserException;
import com.sound.model.User;
import com.sound.model.User.UserEmail;
import com.sound.model.User.UserEmail.EmailSetting;
import com.sound.model.User.UserExternal;
import com.sound.model.User.UserPrefer;
import com.sound.model.User.UserProfile;
import com.sound.model.User.UserRole;
import com.sound.model.User.UserSocial;
import com.sound.model.UserActivity.UserConnect;
import com.sound.model.UserAuth;
import com.sound.model.UserAuth.ChangeHistory;
import com.sound.model.UserMessage;
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

  @Autowired
  UserMessageDAO userMessageDAO;

  @Override
  public User getUserByAlias(String userAlias) {
    User user = userDAO.findOne("profile.alias", userAlias);

    if (user == null) return null;

    if (user.getProfile().hasAvatar()) {
      user.getProfile().setAvatorUrl(
          remoteStorageService.generateDownloadUrl(user.getProfile().getAvatorUrl(),
              FileType.getFileType("image")).toString());
    }

    user.setUserPrefer(getUserPreferOfSound(user, user));

    return user;
  }

  @Override
  public User getUserByEmail(String emailAddress) {
    User user = userDAO.findOne("emails.emailAddress", emailAddress);

    if (user == null) return null;

    if (user.getProfile().hasAvatar()) {
      user.getProfile().setAvatorUrl(
          remoteStorageService.generateDownloadUrl(user.getProfile().getAlias(),
              FileType.getFileType("image")).toString());
    }

    user.setUserPrefer(getUserPreferOfSound(user, user));
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
    UserProfile profile = new UserProfile();
    profile.setAlias(userAlias);

    UserAuth auth = new UserAuth();
    auth.setPassword(password);
    userAuthDAO.save(auth);

    user.setProfile(profile);
    user.setAuth(auth);

    UserRole role = new UserRole();
    role.setRole(Constant.USER_ROLE);
    List<UserRole> roles = new ArrayList<UserRole>();
    roles.add(role);
    user.setUserRoles(roles);

    UserEmail email = new UserEmail();
    email.setEmailAddress(emailAddress);
    email.setConfirmCode(generateConfirmationCode());
    email.setConfirmed(false);
    email.setContact(true);
    email.setSetting(new EmailSetting());
    user.addEmail(email);
    UserSocial social = new UserSocial();
    social.setFollowed(0L);
    social.setFollowing(0L);
    social.setSounds(0L);
    social.setSoundDuration(0L);
    user.setUserSocial(social);

    userDAO.save(user);

    return user;
  }

  @Override
  public User updatePassword(User user, String password, String ip) throws UserException {
    UserAuth auth = user.getAuth();
    if (auth == null) {
      auth = new UserAuth();
      user.setAuth(auth);
      auth.setHistories(new ArrayList<ChangeHistory>());
    }
    auth.setPassword(password);
    ChangeHistory history = new ChangeHistory();
    history.setIp(ip);
    history.setModifiedDate(new Date());
    history.setPassword(password);
    auth.addHistory(history);

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
  public User updateUserBasicProfile(User user, UserProfile newProfile) throws UserException {

    UserProfile profile = user.getProfile();

    if (StringUtils.isNotBlank(newProfile.getAlias())) {
      profile.setAlias(newProfile.getAlias());
    }

    if (StringUtils.isNotBlank(newProfile.getAvatorUrl())) {
      profile.setAvatorUrl(newProfile.getAvatorUrl());
      profile.setHasAvatar(true);
    }

    if (StringUtils.isNotBlank(newProfile.getFirstName())) {
      profile.setFirstName(newProfile.getFirstName());
    }

    if (StringUtils.isNotBlank(newProfile.getLastName())) {
      profile.setLastName(newProfile.getLastName());
    }

    if (StringUtils.isNotBlank(newProfile.getCity())) {
      profile.setCity(newProfile.getCity());
    }

    if (StringUtils.isNotBlank(newProfile.getCountry())) {
      profile.setCountry(newProfile.getCountry());
    }

    if (StringUtils.isNotBlank(newProfile.getDescription())) {
      profile.setDescription(newProfile.getDescription());
    }

    if (CollectionUtils.isNotEmpty(newProfile.getOccupations())) {
      profile.setOccupations(newProfile.getOccupations());
    }

    userDAO.updateProperty("_id", user.getId(), "profile", profile);

    return user;

  }

  @Override
  public User updateUserSnsProfile(User user, UserExternal newExternal) throws UserException {
    UserExternal external = (null == user.getExternal()) ? new UserExternal() : user.getExternal();

    if (StringUtils.isNotBlank(newExternal.getWebsite())) {
      external.setWebsite(newExternal.getWebsite());
    }

    if (StringUtils.isNotBlank(newExternal.getSina())) {
      external.setSina(newExternal.getSina());
    }

    if (StringUtils.isNotBlank(newExternal.getTencent())) {
      external.setTencent(newExternal.getTencent());
    }

    if (StringUtils.isNotBlank(newExternal.getQq())) {
      external.setQq(newExternal.getQq());
    }

    if (StringUtils.isNotBlank(newExternal.getRenren())) {
      external.setRenren(newExternal.getRenren());
    }

    if (StringUtils.isNotBlank(newExternal.getDouban())) {
      external.setDouban(newExternal.getDouban());
    }

    if (StringUtils.isNotBlank(newExternal.getXiami())) {
      external.setXiami(newExternal.getXiami());
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
    email.setContact(false);
    email.setConfirmed(false);
    email.setEmailAddress(emailAddress);
    email.setConfirmCode(generateConfirmationCode());
    email.setSetting(new EmailSetting());
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

  private String generateConfirmationCode() {
    return RandomStringUtils.random(32, true, true);
  }

  private void doSendEmail(String emailAddress, String userAlias, String confirmCode)
      throws UserException {
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
      logger.error(e);
      throw new UserException("Cannot send email confirmation for email " + emailAddress);
    }
  }

  private String generateHtmlEmailBody(String code, String userAlias) {
    StringBuilder sb = new StringBuilder();
    sb.append("<h2>Welcome to Wooice Family</h2>");
    sb.append("Hi " + userAlias + ",<br/><br/>");
    sb.append("We've received a request to add this email address to a Wooice account. Please click ");
    sb.append("<a href=\"http://localhost:8080/commonService/confirmEmail/" + code
        + "\">this link to confirm that it's OK.</a> <br/><br/>");
    sb.append("<h4>The WOOICE Team<h4/>");
    return sb.toString();
  }

  @Override
  public User changeContactEmailAddress(String userAlias, String targetEmailAddress)
      throws UserException {
    User user = this.getUserByAlias(userAlias);
    if (user == null) {
      throw new UserException("Cannot find user : " + userAlias);
    }
    List<UserEmail> emails = user.getEmails();
    for (UserEmail email : emails) {
      if (email.getEmailAddress().equals(targetEmailAddress)) {
        email.setContact(true);
      } else {
        email.setContact(false);
      }
    }
    userDAO.updateProperty("_id", user.getId(), "emails", emails);
    return user;
  }

  @Override
  public User updateEmailSetting(String emailAddress, EmailSetting emailSetting)
      throws UserException {
    User user = this.getUserByEmail(emailAddress);
    if (null == user) {
      throw new UserException("Cannot find user with email address : " + emailAddress);
    }
    List<UserEmail> emails = user.getEmails();
    for (UserEmail email : emails) {
      if (email.getEmailAddress().equals(emailAddress)) {
        email.setSetting(emailSetting);
      }
    }
    userDAO.updateProperty("_id", user.getId(), "emails", emails);

    return user;
  }

  @Override
  public void sendUserMessage(String fromUser, String toUser, String topic, String content)
      throws UserException {
    User from = this.getUserByAlias(fromUser);
    User to = this.getUserByAlias(toUser);
    if (from == null) {
      throw new UserException("Cannot find user : " + fromUser);
    }
    if (to == null) {
      throw new UserException("Cannot find user : " + toUser);
    }

    String summary = content.length() <= 50 ? content : content.substring(0, 49) + "...";

    UserMessage message = new UserMessage();
    message.setFrom(from);
    message.setTo(to);
    message.setTopic(topic);
    message.setContent(content);
    message.setSummary(summary);
    message.setDate(new Date());
    userMessageDAO.save(message);

    from.addOutputMessage(message);
    to.addInputMessage(message);

    userDAO.updateProperty("_id", from.getId(), "outputMessages", from.getOutputMessages());
    userDAO.updateProperty("_id", to.getId(), "inputMessages", to.getInputMessages());

  }

  @Override
  public void removeUserMessage(String fromUser, String toUser, String messageId)
      throws UserException {
    User from = this.getUserByAlias(fromUser);
    User to = this.getUserByAlias(toUser);
    if (from == null) {
      throw new UserException("Cannot find user : " + fromUser);
    }
    if (to == null) {
      throw new UserException("Cannot find user : " + toUser);
    }

    UserMessage message = userMessageDAO.findOne("_id", messageId);
    userMessageDAO.delete(message);

    from.removeOutputMessage(message);
    to.removeInputMessage(message);

    userDAO.updateProperty("_id", from.getId(), "outputMessages", from.getOutputMessages());
    userDAO.updateProperty("_id", to.getId(), "inputMessages", to.getInputMessages());

  }

  @Override
  public User getCurrentUser(HttpServletRequest req) {
    HttpSession session = req.getSession(false);
    String userAlias = (null == session) ? null : (String) session.getAttribute("userAlias");

    return (null == userAlias) ? null : userDAO.findOne("profile.alias", userAlias);
  }

  @Override
  public User grantRole(User user, String role) {
    UserRole userRole = new UserRole();
    userRole.setRole(role);
    List<UserRole> newUserRole = new ArrayList<UserRole>();
    newUserRole.add(userRole);
    user.setUserRoles(newUserRole);

    userDAO.save(user);

    return user;
  }

}

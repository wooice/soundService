package com.sound.service.user.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sound.constant.Constant;
import com.sound.dao.PasswordResetRequestDAO;
import com.sound.dao.UserAuthDAO;
import com.sound.dao.UserConnectDAO;
import com.sound.dao.UserDAO;
import com.sound.dao.UserMessageDAO;
import com.sound.exception.AuthException;
import com.sound.exception.UserException;
import com.sound.model.User;
import com.sound.model.User.UserEmail;
import com.sound.model.User.UserEmail.EmailSetting;
import com.sound.model.User.UserExternal;
import com.sound.model.User.UserExternal.Site;
import com.sound.model.User.UserPrefer;
import com.sound.model.User.UserProfile;
import com.sound.model.User.UserRole;
import com.sound.model.User.UserSocial;
import com.sound.model.UserActivity.UserConnect;
import com.sound.model.UserAuth;
import com.sound.model.UserAuth.ChangeHistory;
import com.sound.model.UserAuth.PasswordResetRequest;
import com.sound.model.UserMessage;
import com.sound.service.storage.itf.RemoteStorageServiceV2;

@Service
@Scope("singleton")
public class UserService implements com.sound.service.user.itf.UserService {

  Logger logger = Logger.getLogger(UserService.class);

  private static final String CONFIG_FILE = "config.properties";

  @Autowired
  UserDAO userDAO;

  @Autowired
  UserConnectDAO userConnectDAO;

  @Autowired
  RemoteStorageServiceV2 remoteStorageService;

  @Autowired
  UserAuthDAO userAuthDAO;

  @Autowired
  UserMessageDAO userMessageDAO;

  @Autowired
  PasswordResetRequestDAO passwordResetRequestDAO;

  private PropertiesConfiguration config;

  public UserService() {
    try {
      config = new PropertiesConfiguration(CONFIG_FILE);
    } catch (ConfigurationException e) {
      e.printStackTrace();
    }
  }


  @Override
  public User getUserByAlias(String userAlias) {
    User user = userDAO.findOne("profile.alias", userAlias);

    if (user == null) return null;

    if (user.getProfile().hasAvatar()) {
      user.getProfile().setAvatorUrl(
          remoteStorageService.getDownloadURL(user.getId().toString(), "image", "format/png"));
    }

    return user;
  }

  @Override
  public User getUserByEmail(String emailAddress) {
    User user = userDAO.findOne("emails.emailAddress", emailAddress);

    if (user == null) return null;

    if (user.getProfile().hasAvatar()) {
      user.getProfile().setAvatorUrl(
          remoteStorageService.getDownloadURL(user.getId().toString(), "image", "format/png"));
    }

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
    profile.setGender("unset");

    UserAuth auth = new UserAuth();
    auth.setPassword(hashPassword(password));
    userAuthDAO.save(auth);

    user.setProfile(profile);
    user.setAuth(auth);

    UserRole role = new UserRole(Constant.USER_ROLE);
    role.setAllowedDuration(Constant.USER_ALLOWED_DURATION);
    List<UserRole> roles = new ArrayList<UserRole>();
    roles.add(role);
    user.setUserRoles(roles);

    UserEmail email = new UserEmail();
    email.setEmailAddress(emailAddress);
    email.setConfirmCode(generateRandomCode());
    email.setConfirmed(false);
    email.setContact(true);
    email.setSetting(new EmailSetting());
    user.addEmail(email);
    UserSocial social = new UserSocial();
    social.setFollowed(0L);
    social.setFollowing(0L);
    social.setSounds(0L);
    social.setSoundDuration(0L);
    social.setReposts(0L);
    user.setUserSocial(social);

    UserExternal userExternal = new UserExternal();
    userExternal.addSite(new Site("site", "个人网站", ""));
    userExternal.addSite(new Site("sina", "新浪微博", ""));
    userExternal.addSite(new Site("tencent", "腾讯微博", ""));
    userExternal.addSite(new Site("renren", "人人网", ""));
    userExternal.addSite(new Site("douban", "豆瓣", ""));
    userExternal.addSite(new Site("xiami", "虾米", ""));
    user.setExternal(userExternal);

    userDAO.save(user);

    return user;
  }

  @Override
  public User updatePassword(String code, String oldPassword, String password, String ip)
      throws UserException, AuthException {
    PasswordResetRequest request = passwordResetRequestDAO.findOne("resetCode", code);
    if (null == request) {
      throw new AuthException("You can't update password.");
    }

    User user = request.getUser();

    if (!user.getAuth().getPassword().equals(hashPassword(oldPassword))) {
      throw new UserException("Old password not match!");
    }

    if (oldPassword.equals(password)) {
      throw new UserException("Password not changed!");
    }

    UserAuth auth = user.getAuth();
    if (auth == null) {
      auth = new UserAuth();
      user.setAuth(auth);
      auth.setHistories(new ArrayList<ChangeHistory>());
    }
    auth.setPassword(hashPassword(password));
    ChangeHistory history = new ChangeHistory();
    history.setIp(ip);
    history.setModifiedDate(new Date());
    history.setPassword(hashPassword(password));
    auth.addHistory(history);

    userAuthDAO.save(auth);

    passwordResetRequestDAO.delete(request);

    return user;
  }

  public void deleteByAlias(String userAlias) {
    userDAO.deleteByProperty("profile.alias", userAlias);
  }

  @Override
  public UserPrefer getUserPrefer(User currentUser, User targetUser) {
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

    if (null != newProfile.getColor()) {
      profile.setColor(newProfile.getColor());
    }

    userDAO.updateProperty("_id", user.getId(), "profile", profile);

    return user;

  }

  @Override
  public User updateUserSnsProfile(User user, UserExternal newExternal) throws UserException {
    userDAO.updateProperty("_id", user.getId(), "external", newExternal);

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
    email.setConfirmCode(generateRandomCode());
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
        doSendEmail("[Wooice]注册确认", emailAddress, userAlias,
            generateConformEmailBody(email.getConfirmCode(), userAlias));
      }
    }
  }

  @Override
  public void sendChangePassLink(String emailAddress) throws UserException {
    User user = this.getUserByEmail(emailAddress);
    if (user == null) {
      throw new UserException("Cannot find user by email " + emailAddress);
    }
    List<UserEmail> emails = user.getEmails();
    for (UserEmail email : emails) {
      if (email.getEmailAddress().equals(emailAddress)) {
        passwordResetRequestDAO.deleteByProperty("user", user);

        PasswordResetRequest passwordResetRequest = new PasswordResetRequest();
        passwordResetRequest.setUser(user);
        passwordResetRequest.setResetCode(this.generateRandomCode());
        passwordResetRequest.setCancelCode(this.generateRandomCode());

        doSendEmail(
            "[Wooice]重置密码",
            emailAddress,
            user.getProfile().getAlias(),
            generateChangePassBody(passwordResetRequest.getResetCode(),
                passwordResetRequest.getCancelCode(), user.getProfile().getAlias()));

        passwordResetRequestDAO.save(passwordResetRequest);
        return;
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

  @Override
  public boolean verifyResetRequest(String action, String code) throws UserException {
    PasswordResetRequest request = null;
    if (action.equals("confirm")) {
      request = passwordResetRequestDAO.findOne("resetCode", code);
      if (request == null) {
        return false;
      }
      return true;
    } else {
      if (action.equals("cancel")) {
        request = passwordResetRequestDAO.findOne("cancelCode", code);
        if (request == null) {
          return false;
        }
        passwordResetRequestDAO.delete(request);
        return true;
      } else {
        throw new UserException("Action " + action + " not supported.");
      }
    }
  }

  private String generateRandomCode() {
    return RandomStringUtils.random(32, true, true);
  }

  private void doSendEmail(String title, String emailAddress, String userAlias, String body)
      throws UserException {
    try {
      HtmlEmail email = new HtmlEmail();
      email.setAuthentication("wooice", "dxd123456");
      email.setCharset("utf-8");
      email.setSubject(title);
      email.setHostName("smtp.126.com");
      email.setFrom("wooice@126.com", "Wooice");
      email.addTo(emailAddress, userAlias);
      email.setContent(body, "text/html; charset=UTF-8");
      email.send();
    } catch (EmailException e) {
      logger.error(e);
      throw new UserException("Cannot send email confirmation for email " + emailAddress);
    }
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
  public void sendUserMessage(User fromUser, User toUser, String topic, String content)
      throws UserException {
    String summary = content.length() <= 50 ? content : content.substring(0, 49) + "...";

    UserMessage message = new UserMessage();
    message.setFrom(fromUser);
    message.setTo(toUser);
    message.setTopic(topic);
    message.setContent(content);
    message.setSummary(summary);
    message.setDate(new Date());
    message.setStatus("unread");
    userMessageDAO.save(message);

    if (null != fromUser) {
      userDAO.increase("profile.alias", fromUser.getProfile().getAlias(),
          "userSocial.outputMessages");
    }
    userDAO.increase("profile.alias", toUser.getProfile().getAlias(), "userSocial.inputMessages");
  }

  @Override
  public List<UserMessage> getUserMessages(User toUser, String status, Integer pageNum,
      Integer perPage) {
    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("to", toUser);

    if (null != status) {
      cratiaries.put("status", status);
    }

    return userMessageDAO.findWithRange(cratiaries, (pageNum - 1) * perPage, perPage, "-date");
  }

  @Override
  public void markUserMessage(String messageId, String status) throws UserException {
    UserMessage userMessage = userMessageDAO.findOne("_id", new ObjectId(messageId));
    if (status.equals("delete")) {
      if (null != userMessage.getFrom()) {
        userDAO.decrease("profile.alias", userMessage.getFrom().getProfile().getAlias(),
            "userSocial.outputMessages");
      }
      userDAO.decrease("profile.alias", userMessage.getTo().getProfile().getAlias(),
          "userSocial.inputMessages");
    }

    if (!status.equals("trash")) {
      userMessageDAO.updateProperty("_id", userMessage.getId(), "status", status);
    } else {
      userMessageDAO.deleteByProperty("_id", userMessage.getId());
    }
  }

  @Override
  public User getCurrentUser(HttpServletRequest req) {
    HttpSession session = req.getSession(false);
    String userAlias = (null == session) ? null : (String) session.getAttribute("userAlias");

    return (null == userAlias) ? null : userDAO.findOne("profile.alias", userAlias);
  }

  @Override
  public User grantRole(User user, String role) {
    UserRole userRole = new UserRole(role);

    List<UserRole> newUserRole = new ArrayList<UserRole>();
    newUserRole.add(userRole);
    user.setUserRoles(newUserRole);

    userDAO.save(user);

    return user;
  }

  private String hashPassword(String password) {
    byte[] passHash = null;
    try {
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
      passHash = sha256.digest(password.getBytes());
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    return new String(passHash);
  }

  private String generateConformEmailBody(String code, String userAlias) {
    StringBuilder sb = new StringBuilder();
    sb.append("<h2>感谢您注册Wooice!</h2>");
    sb.append("Hi " + userAlias + ",<br/><br/>");
    sb.append("感谢您注册Wooice，请点击以下面链接 ");
    sb.append("<a href=\"" + config.getString("site") + "#/auth/confirm?confirmCode=" + code
        + "\">激活您的账号.</a> <br/><br/>");
    sb.append("<h4>WOOICE团队<h4/>");
    return sb.toString();
  }

  private String generateChangePassBody(String changeCode, String cancelCode, String userAlias) {
    StringBuilder sb = new StringBuilder();
    sb.append("<h3>请修改您的密码</h3>");
    sb.append("Hi " + userAlias + ",<br/><br/>");
    sb.append("我们收到您修改密码的请求，请访问以下链接 ");
    sb.append("<a href=\"" + config.getString("site") + "#/auth/confirm?resetCode=" + changeCode
        + "\">修改密码</a> <br/><br/>");
    sb.append("如果您没有发出修改密码请求，请访问以下连续取消修改 ");
    sb.append("<a href=\"" + config.getString("site") + "#/auth/confirm?cancelCode=" + cancelCode
        + "\">取消修改</a> <br/><br/>");
    sb.append("<h4>WOOICE团队<h4/>");
    return sb.toString();
  }


  @Override
  public boolean authVerify(User user, String password) {
    return user.getAuth().getPassword().equals(hashPassword(password));
  }
}

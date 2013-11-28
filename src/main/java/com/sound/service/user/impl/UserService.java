package com.sound.service.user.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
import com.sound.service.storage.itf.RemoteStorageService;

@Service
@Scope("singleton")
public class UserService implements com.sound.service.user.itf.UserService {

  Logger logger = Logger.getLogger(UserService.class);

  private static final String CONFIG_FILE = "config.properties";
  private static final String STORAGE_CONFIG_FILE = "storeConfig.properties";

  char[] codeSequence = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
      'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6',
      '7', '8', '9'};

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

  private PropertiesConfiguration config;
  private PropertiesConfiguration storageConfig;

  public UserService() {
    try {
      config = new PropertiesConfiguration(CONFIG_FILE);
      storageConfig = new PropertiesConfiguration(STORAGE_CONFIG_FILE);
    } catch (ConfigurationException e) {
      e.printStackTrace();
    }
  }


  @Autowired
  PasswordResetRequestDAO passwordResetRequestDAO;

  @Override
  public User getUserByAlias(String userAlias) {
    User user = userDAO.findOne("profile.alias", userAlias);

    if (user == null) return null;

    if (user.getProfile().hasAvatar()) {
      user.getProfile().setAvatorUrl(
          remoteStorageService.getDownloadURL(user.getId().toString(), "image",
              "imageView/2/w/200/h/200/format/png"));
    }

    return user;
  }

  @Override
  public User getUserByEmail(String emailAddress) {
    User user = userDAO.findOne("emails.emailAddress", emailAddress);

    if (user == null) return null;

    if (user.getProfile().hasAvatar()) {
      user.getProfile().setAvatorUrl(
          remoteStorageService.getDownloadURL(user.getId().toString(), "image",
              "imageView/2/w/200/h/200/format/png"));
    }

    return user;
  }

  @Override
  public User createUser(String userAlias, String emailAddress, String password)
      throws UserException {
    if (StringUtils.isBlank(userAlias)) {
      throw new UserException("ALIAS_NOT_NULL");
    }

    if (StringUtils.isBlank(password)) {
      throw new UserException("PASSWORD_NOT_NULL");
    }

    User user = this.getUserByAlias(userAlias);

    if (null != user) {
      throw new UserException("ALIAS_DUPLICATE");
    }

    user = this.getUserByEmail(emailAddress);

    if (null != user) {
      throw new UserException("EMAIL_DUPLICATE");
    }

    user = new User();
    UserProfile profile = new UserProfile();
    profile.setAlias(userAlias);
    profile.setGender("unset");
    profile.setCreateDate(new Date());

    UserAuth auth = new UserAuth();
    auth.setSalt(String.valueOf(Math.random() * 1000));
    auth.setPassword(hashPassword(password, auth.getSalt()));
    auth.setAuthToken(hashPassword("auth token:" + (int) (Math.random() * 1000), auth.getSalt()));
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
    userExternal.addSite(new Site("qq", "QQ", "", false));
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

    if (!user.getAuth().getPassword().equals(hashPassword(oldPassword, user.getAuth().getSalt()))) {
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
    auth.setPassword(hashPassword(password, user.getAuth().getSalt()));
    auth.setAuthToken(hashPassword("auth token:" + (int) (Math.random() * 1000), user.getAuth()
        .getSalt()));

    ChangeHistory history = new ChangeHistory();
    history.setIp(ip);
    history.setModifiedDate(new Date());
    history.setPassword(hashPassword(password, auth.getPassword()));
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

    if (StringUtils.isNotBlank(newProfile.getAvatorUrl()) && newProfile.getAvatorUrl().contains(storageConfig.getString("IMAGE_DOMAIN"))) {
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
      throw new UserException("USER_404");
    }
    List<UserEmail> emails = user.getEmails();
    for (UserEmail email : emails) {
      if (email.getEmailAddress().equals(emailAddress) && email.isContact()) {
        if (email.isConfirmed()) {
          throw new UserException("CONFIRMED");
        } else {
          doSendEmail("[Wowoice]注册确认", emailAddress, userAlias,
              generateConformEmailBody(email.getConfirmCode(), userAlias));
          return;
        }
      }
    }

    throw new UserException("VALID_EMAIL_404");
  }

  @Override
  public void sendChangePassLink(String emailAddress) throws UserException {
    User user = this.getUserByEmail(emailAddress);
    if (user == null) {
      throw new UserException("USER_404");
    }
    List<UserEmail> emails = user.getEmails();
    for (UserEmail email : emails) {
      if (email.getEmailAddress().equals(emailAddress) && email.isContact() && email.isConfirmed()) {
        passwordResetRequestDAO.deleteByProperty("user", user);

        PasswordResetRequest passwordResetRequest = new PasswordResetRequest();
        passwordResetRequest.setUser(user);
        passwordResetRequest.setResetCode(this.generateRandomCode());
        passwordResetRequest.setCancelCode(this.generateRandomCode());

        doSendEmail(
            "[e]重置密码",
            emailAddress,
            user.getProfile().getAlias(),
            generateChangePassBody(passwordResetRequest.getResetCode(),
                passwordResetRequest.getCancelCode(), user.getProfile().getAlias()));

        passwordResetRequestDAO.save(passwordResetRequest);
        return;
      }
    }

    throw new UserException("VALID_EMAIL_404");
  }

  @Override
  public void confirmEmailAddress(String confirmCode) throws UserException {
    User user = userDAO.findOne("emails.confirmCode", confirmCode);
    if (user == null) {
      throw new UserException("USER_404");
    }
    boolean found = false;
    List<UserEmail> emails = user.getEmails();
    for (UserEmail email : emails) {
      if (email.isContact() && email.getConfirmCode().equals(confirmCode)) {
        if (email.isConfirmed()) {
          throw new UserException("CONFIRMED");
        } else {
          found = true;
          email.setConfirmed(true);
        }
      }
    }

    if (!found) {
      throw new UserException("TO_CONFIRM_404");
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
      throw new UserException("USER_404");
    }
    UserEmail newEmail = null;
    List<UserEmail> emails = user.getEmails();

    for (UserEmail email : emails) {
      if (email.getEmailAddress().equals(targetEmailAddress)) {
        if (email.isContact() && email.isConfirmed()) {
          throw new UserException("CONFIRMED");
        }
        newEmail = email;
      } else {
        email.setContact(false);
      }
    }

    if (null == newEmail) {
      newEmail = new UserEmail();
      newEmail.setEmailAddress(targetEmailAddress);
      newEmail.setConfirmCode(generateRandomCode());
      newEmail.setConfirmed(false);
      newEmail.setContact(true);
      newEmail.setSetting(new EmailSetting());
      emails.add(newEmail);
    } else {
      newEmail.setConfirmCode(generateRandomCode());
      newEmail.setConfirmed(false);
      newEmail.setContact(true);
    }
    userDAO.updateProperty("_id", user.getId(), "emails", emails);

    doSendEmail("[Wowoice]添加新邮箱", targetEmailAddress, userAlias,
        changeEmailBody(newEmail.getConfirmCode(), userAlias));

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
  public void sendUserMessage(User fromUser, User toUser, String topic, String content) {
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

  private String hashPassword(String password, String salt) {
    if (!StringUtils.isBlank(salt)) {
      password += salt;
    }
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
    sb.append("<h2>感谢您注册Wowoice!</h2>");
    sb.append("Hi " + userAlias + ",<br/><br/>");
    sb.append("感谢您注册Wowoice，请点击以下面链接 ");
    sb.append("<a href=\"" + config.getString("site") + "#/auth/confirm?confirmCode=" + code
        + "\">激活您的账号.</a> <br/><br/>");
    sb.append("<h4>WOWOICE<h4/>");
    return sb.toString();
  }

  private String changeEmailBody(String code, String userAlias) {
    StringBuilder sb = new StringBuilder();
    sb.append("<h2>添加新邮箱到Wowoice</h2>");
    sb.append("Hi " + userAlias + ",<br/><br/>");
    sb.append("Wowoice收到您的邮件变更请求，请点击以下面链接 ");
    sb.append("<a href=\"" + config.getString("site") + "#/auth/confirm?confirmCode=" + code
        + "\">确认.谢谢合作。</a> <br/><br/>");
    sb.append("<h4>WOWOICE<h4/>");
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
    sb.append("<h4>WOWOICE<h4/>");
    return sb.toString();
  }

  @Override
  public boolean authVerify(User user, String password) {
    return user.getAuth().getPassword().equals(hashPassword(password, user.getAuth().getSalt()));
  }

  @Override
  public User saveUser(User user) {
    userDAO.save(user);
    return user;
  }

  @Override
  public boolean tokenVerify(User user, String token) {
    return user.getAuth().getAuthToken().equals(token);
  }


  @Override
  public BufferedImage generateVerifyImage(HttpServletRequest req) {
    int width = 85, height = 20;
    Random random = new Random();
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics g = image.getGraphics();
    g.setColor(getRandColor(200, 250));
    g.fillRect(0, 0, width, height);
    g.setFont(new Font("Times New Roman", Font.PLAIN, 18));
    g.setColor(getRandColor(160, 200));

    // 随机产生155条干扰线
    for (int i = 0; i < 155; i++) {
      int x = random.nextInt(width);
      int y = random.nextInt(height);
      int xl = random.nextInt(12);
      int yl = random.nextInt(12);
      g.drawLine(x, y, x + xl, y + yl);
    }

    String code = "";
    for (int i = 0; i < 5; i++) {
      String randomString = String.valueOf(codeSequence[random.nextInt(36)]);
      g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random
          .nextInt(110)));
      g.drawString(randomString, 13 * i + 6, 16);

      code += randomString;
    }
    g.dispose();

    HttpSession session = req.getSession(true);
    session.setAttribute("verifyCode", code);

    return image;
  }

  private Color getRandColor(int fc, int bc) {
    Random random = new Random();
    if (fc > 255) fc = 255;
    if (bc > 255) bc = 255;
    int r = fc + random.nextInt(bc - fc);
    int g = fc + random.nextInt(bc - fc);
    int b = fc + random.nextInt(bc - fc);
    return new Color(r, g, b);
  }

  @Override
  public User syncExternalUser(User user, String type) throws UserException {
    Site newSite = null;
    for (Site site : user.getExternal().getSites()) {
      if (site.getName().equals(type)) {
        newSite = site;
      }
    }
    if (null == newSite) {
      throw new UserException("SITE_NULL");
    }
    if (null == newSite.getUid()) {
      throw new UserException("UID_NULL");
    }
    if (null == newSite.getUserName()) {
      throw new UserException("USERNAME_NULL");
    }

    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("external.sites.uid", newSite.getUid());
    User exsitingUser = userDAO.findOne(cratiaries);

    if (null == exsitingUser) {
      if (userDAO.count("profile.alias", newSite.getUserName()) > 0) {
        if (userDAO.count("profile.alias", newSite.getUserName() + user.getProfile().getAge()) > 0) {
          Random random = new Random(1000);
          String alias = null;
          do {
            alias = newSite.getUserName() + random.nextInt();
          } while (userDAO.count("profile.alias", alias) > 0);
          user.getProfile().setAlias(alias);
        } else {
          user.getProfile().setAlias(newSite.getUserName() + user.getProfile().getAge());
        }
      } else {
        user.getProfile().setAlias(newSite.getUserName());
      }
      
      user.getProfile().setCreateDate(new Date());

      UserAuth auth = new UserAuth();
      auth.setSalt(String.valueOf(Math.random() * 1000));
      auth.setPassword(hashPassword(String.valueOf(Math.random() * 1000), auth.getSalt()));
      auth.setAuthToken(hashPassword("auth token:" + (int) (Math.random() * 1000), auth.getSalt()));
      userAuthDAO.save(auth);
      user.setAuth(auth);

      UserRole role = new UserRole(Constant.USER_ROLE);
      role.setAllowedDuration(Constant.USER_ALLOWED_DURATION);
      List<UserRole> roles = new ArrayList<UserRole>();
      roles.add(role);
      user.setUserRoles(roles);

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
      userExternal.addSite(new Site("qq", "QQ", "", false));
      user.setExternal(userExternal);
      user.getExternal().updateSite(newSite);

      userDAO.save(user);
    } else {
      if (null != user.getProfile().getAvatorUrl()) {
        exsitingUser.getProfile().setAvatorUrl(user.getProfile().getAvatorUrl());
      }
      if (null != user.getProfile().getCity()) {
        exsitingUser.getProfile().setCity(user.getProfile().getCity());
      }
      if (null != user.getProfile().getDescription()) {
        exsitingUser.getProfile().setDescription(user.getProfile().getDescription());
      }
      if (null != user.getProfile().getGender()) {
        exsitingUser.getProfile().setGender(user.getProfile().getGender());
      }
      exsitingUser.getExternal().updateSite(newSite);

      userDAO.save(exsitingUser);
      user = exsitingUser;
    }

    return user;
  }
}

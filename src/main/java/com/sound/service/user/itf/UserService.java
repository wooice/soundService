package com.sound.service.user.itf;

import java.awt.image.BufferedImage;

import javax.servlet.http.HttpServletRequest;

import com.sound.exception.AuthException;
import com.sound.exception.UserException;
import com.sound.model.User;
import com.sound.model.User.UserEmail.EmailSetting;
import com.sound.model.User.UserExternal;
import com.sound.model.User.UserPrefer;
import com.sound.model.User.UserProfile;

public interface UserService {

  public User getUserByEmail(String userAlias) throws UserException;

  public User createUser(String userAlias, String emailAddress, String password)
      throws UserException;

  public User updatePassword(String code, String oldPassword, String password, String ip)
      throws UserException, AuthException;
  
  public User resetPassword(String code, String password, String ip) throws UserException, AuthException;

  public User getUserByAlias(String userAlias);

  public void deleteByAlias(String userAlias);

  public User updateUserBasicProfile(User user, UserProfile profile) throws UserException;

  public User updateUserSnsProfile(User user, UserExternal external) throws UserException;

  public User addEmailAddress(String userAlias, String emailAddress) throws UserException;

  public void sendEmailAddressConfirmation(User user, String emailAddress)
      throws UserException;

  public void confirmEmailAddress(String confirmCode) throws UserException;

  public String changeContactEmailAddress(User curUser, String targetEmailAddress)
      throws UserException;

  public User updateEmailSetting(String emailAddress, EmailSetting emailSetting)
      throws UserException;

  public User getCurrentUser(HttpServletRequest req);

  public User grantRole(User user, String role);

  public void sendChangePassLink(String emailAddress) throws UserException;

  public boolean verifyResetRequest(String action, String code) throws UserException;

  public boolean authVerify(User user, String password);
  
  public boolean tokenVerify(User user, String token);

  public UserPrefer getUserPrefer(User currentUser, User targetUser);
  
  public User saveUser(User user);

  public BufferedImage generateVerifyImage(HttpServletRequest req);

  public User syncExternalUser(User user, String type) throws UserException;
}

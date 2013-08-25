package com.sound.service.user.itf;

import javax.servlet.http.HttpServletRequest;

import com.sound.exception.UserException;
import com.sound.model.User;
import com.sound.model.User.UserEmail.EmailSetting;
import com.sound.model.User.UserExternal;
import com.sound.model.User.UserProfile;

public interface UserService {

	public User getUserByEmail(String userAlias) throws UserException;

	public User createUser(String userAlias, String emailAddress, String password) throws UserException;
	
	public User updatePassword(String emailAddress, String password, String ip) throws UserException;

	public User getUserByAlias(String userAlias);
	
	public void deleteByAlias(String userAlias);
	
	public User updateUserBasicProfile(String userAlias, UserProfile profile) throws UserException;

	public User updateUserSnsProfile(String userAlias, UserExternal external) throws UserException;

	public User addEmailAddress(String userAlias, String emailAddress) throws UserException;
	  
	public void sendEmailAddressConfirmation(String userAlias, String emailAddress) throws UserException;
	  
	public void confirmEmailAddress(String confirmCode) throws UserException;
	
	public User changeContactEmailAddress(String userAlias, String targetEmailAddress) throws UserException;
	
	public User updateEmailSetting(String emailAddress, EmailSetting emailSetting) throws UserException;
	
	public void sendUserMessage(String fromUser, String toUser, String topic, String content) throws UserException;

	public void removeUserMessage(String fromUser, String toUser, String messageId) throws UserException;
	
	public User getCurrentUser(HttpServletRequest req);
	
	public User grantRole(User user, String role);
}

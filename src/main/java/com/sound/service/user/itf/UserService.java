package com.sound.service.user.itf;

import com.sound.dto.UserBasicProfileDTO;
import com.sound.dto.UserSnsProfileDTO;
import com.sound.exception.UserException;
import com.sound.model.User;

public interface UserService {

	public User getUserByEmail(String userAlias) throws UserException;

	public User createUser(String userAlias, String emailAddress, String password) throws UserException;
	
	public User updatePassword(String emailAddress, String password, String ip) throws UserException;

	public User getUserByAlias(String userAlias);
	
	public void deleteByAlias(String userAlias);
	
	public User updateUserBasicProfile(String userAlias, UserBasicProfileDTO profileDTO) throws UserException;

	public User updateUserSnsProfile(String userAlias, UserSnsProfileDTO snsDTO) throws UserException;
}

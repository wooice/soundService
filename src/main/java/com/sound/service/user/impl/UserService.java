package com.sound.service.user.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sound.dao.UserAuthDAO;
import com.sound.dao.UserConnectDAO;
import com.sound.dao.UserDAO;
import com.sound.exception.UserException;
import com.sound.model.User;
import com.sound.model.User.UserExternal;
import com.sound.model.User.UserPrefer;
import com.sound.model.User.UserProfile;
import com.sound.model.UserActivity.UserConnect;
import com.sound.model.UserAuth;
import com.sound.model.UserAuth.ChangeHistory;
import com.sound.model.UserBasicProfileDTO;
import com.sound.model.UserSnsProfileDTO;
import com.sound.model.enums.FileType;
import com.sound.service.storage.itf.RemoteStorageService;

@Service
@Scope("singleton")
public class UserService implements com.sound.service.user.itf.UserService {

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

		if (user.getProfile().hasAvatar()) {
			user.getProfile().setAvatorUrl(
					remoteStorageService.generateDownloadUrl(
							user.getProfile().getAlias(),
							FileType.getFileType("image")).toString());
		}

		user.setUserPrefer(getUserPreferOfSound(user, user));

		return user;
	}

	@Override
	public User getUserByEmail(String emailAddress) throws UserException {
		User.UserEmail userEmail = new User.UserEmail();
		userEmail.setEmailAddress(emailAddress);
		List<User> users = userDAO.fetchEntitiesPropertyContains("emails",
				userEmail);
		if (users.size() > 1) {
			throw new UserException("Find more than 1 users with email : "
					+ emailAddress);
		} else if (users.size() == 0) {
			throw new UserException("Cannot find user by email : "
					+ emailAddress);
		}

		return users.get(0);
	}

	@Override
	public User createUser(String userAlias, String emailAddress,
			String password) throws UserException {
		User user = this.getUserByAlias(userAlias);

		if (null != user) {
			throw new UserException("User with alias " + userAlias + " exists.");
		}

		user = this.getUserByEmail(emailAddress);

		if (null != user) {
			throw new UserException("User with email address " + emailAddress
					+ " exists.");
		}

		user = new User();
		User.UserProfile profile = new User.UserProfile();
		profile.setAlias(userAlias);
		profile.setPassword(password);
		User.UserEmail email = new User.UserEmail();
		email.setEmailAddress(emailAddress);
		user.setProfile(profile);
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

		userDAO.updateProperty("profile.alias", userAlias, "profile", profile);

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

		userDAO.updateProperty("profile.alias", userAlias, "external", external);

		return user;

	}
}

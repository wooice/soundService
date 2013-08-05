package com.sound.service.user.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sound.dao.GroupDAO;
import com.sound.dao.UserConnectDAO;
import com.sound.dao.UserDAO;
import com.sound.exception.SoundException;
import com.sound.exception.UserException;
import com.sound.model.Group;
import com.sound.model.Sound;
import com.sound.model.Tag;
import com.sound.model.User;
import com.sound.model.UserActivity.UserConnect;
import com.sound.service.sound.itf.TagService;
import com.sound.util.SocialUtils;

@Service
@Scope("singleton")
public class UserSocialService implements
		com.sound.service.user.itf.UserSocialService {

	@Autowired
	UserDAO userDAO;

	@Autowired
	UserConnectDAO userConnectDAO;

	@Autowired
	GroupDAO groupDAO;

	@Autowired
	TagService tagService;

	@Override
	public void follow(String fromUserAlias, String toUserAlias)
			throws UserException {
		Map<String, Object> cratiaries = new HashMap<String, Object>();
		cratiaries.put("fromUser.profile.alias", fromUserAlias);
		cratiaries.put("toUser.profile.alias", toUserAlias);
		UserConnect userConnected = userConnectDAO.findOne(cratiaries);

		if (userConnected != null) {
			throw new UserException("The user " + fromUserAlias
					+ "has followed user " + toUserAlias);
		}

		User fromUser = userDAO.findOne("profile.alias", fromUserAlias);
		if (null == fromUser) {
			throw new UserException("The user " + fromUserAlias + "not found.");
		}

		User toUser = userDAO.findOne("profile.alias", toUserAlias);
		if (null == toUser) {
			throw new UserException("The user " + toUserAlias + "not found.");
		}

		UserConnect userConnect = new UserConnect();
		userConnect.setFromUser(fromUser);
		userConnect.setToUser(toUser);
		userConnect.setCreatedTime(new Date());

		userConnectDAO.save(userConnect);

		userDAO.increase("profile.alias", fromUserAlias, "social.following");
		userDAO.increase("profile.alias", toUserAlias, "social.followed");
	}

	@Override
	public void unfollow(String fromUserAlias, String toUserAlias)
			throws UserException {
		Map<String, Object> cratiaries = new HashMap<String, Object>();
		cratiaries.put("fromUser.profile.alias", fromUserAlias);
		cratiaries.put("toUser.profile.alias", toUserAlias);
		UserConnect userConnected = userConnectDAO.findOne(cratiaries);

		if (userConnected == null) {
			throw new UserException("The user " + fromUserAlias
					+ "hasn't followed user " + toUserAlias);
		}

		userConnectDAO.delete(userConnected);

		userDAO.decrease("profile.alias", fromUserAlias, "following");
		userDAO.decrease("profile.alias", toUserAlias, "followed");
	}

	@Override
	public void createGroup(String userAlias, String groupName,
			String description) throws UserException {
		User user = userDAO.findOne("profile.alias", userAlias);

		if (null == user) {
			throw new UserException("User " + userAlias + " not found.");
		}
		Group group = new Group();
		group.setName(groupName);
		group.setDescription(description);
		group.setOwner(user);
		groupDAO.save(group);

		user.addGroup(group);
		userDAO.updateProperty("profile.alias", userAlias, "groups",
				user.getGroups());
	}

	@Override
	public void dismissGroup(String userAlias, String groupName)
			throws UserException {
		User user = userDAO.findOne("profile.alias", userAlias);

		if (null == user) {
			throw new UserException("User " + userAlias + " not found.");
		}

		Group group = groupDAO.findOne("name", groupName);
		if (group.getOwner().getProfile().getAlias() == userAlias) {
			List<User> users = userDAO.find("group.name", groupName);
			for (User groupUser : users) {
				groupUser.removeGroup(group);
				userDAO.updateProperty("profile.alias", groupUser.getProfile()
						.getAlias(), "groups", groupUser.getGroups());
			}

			groupDAO.delete(group);
		} else {
			throw new UserException("User " + userAlias
					+ " don't have rights to delete the group " + groupName);
		}
	}

	@Override
	public void joinGroup(String userAlias, String groupName)
			throws UserException {
		User user = userDAO.findOne("profile.alias", userAlias);

		if (null == user) {
			throw new UserException("User " + userAlias + " not found.");
		}

		Group group = groupDAO.findOne("name", groupName);
		user.addGroup(group);
		userDAO.updateProperty("profile.alias", userAlias, "groups",
				user.getGroups());
	}

	@Override
	public void leaveGroup(String userAlias, String groupName)
			throws UserException {
		User user = userDAO.findOne("profile.alias", userAlias);

		if (null == user) {
			throw new UserException("User " + userAlias + " not found.");
		}

		Group group = groupDAO.findOne("name", groupName);
		user.removeGroup(group);
		userDAO.updateProperty("profile.alias", userAlias, "groups",
				user.getGroups());
	}

	@Override
	public void promoteGroupAdmin(String ownerAlias, String adminAlias,
			String groupName) throws UserException {
		User owner = userDAO.findOne("profile.alias", ownerAlias);
		if (null == owner) {
			throw new UserException("User " + ownerAlias + " not found.");
		}

		User admin = userDAO.findOne("profile.alias", adminAlias);
		if (null == admin) {
			throw new UserException("User " + adminAlias + " not found.");
		}

		Group group = groupDAO.findOne("name", groupName);
		if (group.getOwner().getProfile().getAlias() == ownerAlias) {
			group.addAdmin(admin);
			groupDAO.updateProperty("name", groupName, "admins",
					group.getAdmins());
		} else {
			throw new UserException("User " + ownerAlias
					+ " don't have rights to promote admin of the group "
					+ groupName);
		}
	}

	@Override
	public void demoteGroupAdmin(String ownerAlias, String adminAlias,
			String groupName) throws UserException {
		User owner = userDAO.findOne("profile.alias", ownerAlias);
		if (null == owner) {
			throw new UserException("User " + ownerAlias + " not found.");
		}

		User admin = userDAO.findOne("profile.alias", adminAlias);
		if (null == admin) {
			throw new UserException("User " + adminAlias + " not found.");
		}

		Group group = groupDAO.findOne("name", groupName);
		if (group.getOwner().getProfile().getAlias() == ownerAlias) {
			group.removeAdmin(admin);
			groupDAO.updateProperty("name", groupName, "admins",
					group.getAdmins());
		} else {
			throw new UserException("User " + ownerAlias
					+ " don't have rights to promote admin of the group "
					+ groupName);
		}
	}

	@Override
	public List<User> getFollowedUsers(String toUserAlias, Integer pageNum,
			Integer pageSize) throws UserException {
		Map<String, String> cratiaries = new HashMap<String, String>();
		cratiaries.put("toUser.profile.alias", toUserAlias);
		List<UserConnect> list = userConnectDAO.find(cratiaries);
		List<User> result = new ArrayList<User>();
		for (UserConnect uc : list) {
			result.add(userDAO.findOne("profile.alias", uc.getFromUser()));
		}
		return SocialUtils.sliceList(result, pageNum, pageSize);
	}

	@Override
	public List<User> getFollowingUsers(String fromUserAlias, Integer pageNum,
			Integer pageSize) throws UserException {
		Map<String, String> cratiaries = new HashMap<String, String>();
		cratiaries.put("fromUser.profile.alias", fromUserAlias);
		List<UserConnect> list = userConnectDAO.find(cratiaries);
		List<User> result = new ArrayList<User>();
		for (UserConnect uc : list) {
			result.add(userDAO.findOne("profile.alias", uc.getToUser()));
		}
		return SocialUtils.sliceList(result, pageNum, pageSize);
	}

	public UserConnectDAO getUserConnectDAO() {
		return userConnectDAO;
	}

	public void setUserConnectDAO(UserConnectDAO userConnectDAO) {
		this.userConnectDAO = userConnectDAO;
	}

	public UserDAO getUserDAO() {
		return userDAO;
	}

	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}

	public GroupDAO getGroupDAO() {
		return groupDAO;
	}

	public void setGroupDAO(GroupDAO groupDAO) {
		this.groupDAO = groupDAO;
	}

	public TagService getTagService() {
		return tagService;
	}

	public void setTagService(TagService tagService) {
		this.tagService = tagService;
	}

	@Override
	public List<User> recommandUsersByTags(List<String> tagLabels,
			Integer pageNum, Integer pageSize) throws UserException,
			SoundException {
		Map<User, Long> userFollowedNumMap = new HashMap<User, Long>();
		Map<User, Long> userTagNumMap = this.getUserTagNumMap(tagLabels);

		Map<User, Integer> userTagSeq = SocialUtils.toSeqMap(SocialUtils
				.sortMapByValue(userTagNumMap, false));
		Map<User, Integer> userFollowdSeq = SocialUtils.toSeqMap(SocialUtils
				.sortMapByValue(userFollowedNumMap, false));

		List<User> allResult = SocialUtils.combineLogicAndSocial(userTagSeq,
				userFollowdSeq, SocialUtils.DEFAULT_SOCIAL_POWER);

		return SocialUtils.sliceList(allResult, pageNum, pageSize);
	}

	@Override
	public List<Group> recommandGroupsByTags(List<String> tagLabels,
			Integer pageNum, Integer pageSize) throws UserException,
			SoundException {
		Map<User, Long> userTagNumMap = this.getUserTagNumMap(tagLabels);
		Map<Group, Long> groupHitMap = new HashMap<Group, Long>();

		for (User user : userTagNumMap.keySet()) {
			if (user.getGroups() == null) {
				continue;
			}
			for (Group group : user.getGroups()) {
				if (groupHitMap.containsKey(group)) {
					groupHitMap.put(group, groupHitMap.get(group)
							+ userTagNumMap.get(user));
				} else {
					groupHitMap.put(group, userTagNumMap.get(user));
				}
			}
		}

		List<Group> allResult = SocialUtils.toSeqList(SocialUtils
				.sortMapByValue(groupHitMap, false));
		return SocialUtils.sliceList(allResult, pageNum, pageSize);
	}

	private Map<User, Long> getUserTagNumMap(List<String> tagLabels)
			throws SoundException {
		Map<Tag, List<Sound>> tagSoundMap = new HashMap<Tag, List<Sound>>();
		Map<User, Long> userTagNumMap = new HashMap<User, Long>();

		// fetch tag : sounds map
		for (String label : tagLabels) {
			tagSoundMap.put(tagService.getOrCreate(label, null),
					tagService.getSoundsWithTag(label));
		}

		// get user : number of target tags
		for (Tag tag : tagSoundMap.keySet()) {
			List<Sound> soundsOfTag = tagSoundMap.get(tag);
			for (Sound soundOfTag : soundsOfTag) {
				User user = soundOfTag.getProfile().getOwner();
				if (userTagNumMap.containsKey(user)) {
					userTagNumMap.put(user, userTagNumMap.get(user) + 1);
				} else {
					Set<Tag> tagSet = new HashSet<Tag>();
					tagSet.add(tag);
					userTagNumMap.put(user, (long) 1);
				}
			}
		}

		return userTagNumMap;
	}

	public static void main(String[] args) {
		Map<String, Long> map = new TreeMap<String, Long>();

		map.put("a", 4l);
		map.put("b", 2l);
		map.put("c", 5l);
		map.put("d", 1l);

		List<Entry<String, Long>> result = SocialUtils.sortMapByValue(map,
				false);
		for (Entry<String, Long> entry : result) {
			System.out.println(entry);
		}
	}
}

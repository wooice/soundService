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
	
	@Autowired
	SoundSocialService soundSocialService;

	@Override
	public Integer follow(String fromUserAlias, String toUserAlias)
			throws UserException {
		User fromUser = userDAO.findOne("profile.alias", fromUserAlias);
		if (null == fromUser) {
			throw new UserException("The user " + fromUserAlias + " not found.");
		}

		User toUser = userDAO.findOne("profile.alias", toUserAlias);
		if (null == toUser) {
			throw new UserException("The user " + toUserAlias + " not found.");
		}

		Map<String, Object> cratiaries = new HashMap<String, Object>();
		cratiaries.put("fromUser", fromUser);
		cratiaries.put("toUser", toUser);
		UserConnect userConnected = userConnectDAO.findOne(cratiaries);

		if (userConnected != null) {
			throw new UserException("The user " + fromUserAlias
					+ " has followed user " + toUserAlias);
		}

		UserConnect userConnect = new UserConnect();
		userConnect.setFromUser(fromUser);
		userConnect.setToUser(toUser);
		userConnect.setCreatedTime(new Date());

		userConnectDAO.save(userConnect);

		userDAO.increase("profile.alias", fromUserAlias, "social.following");
		userDAO.increase("profile.alias", toUserAlias, "social.followed");

		return (int) (toUser.getSocial().getFollowed() + 1);
	}

	@Override
	public Integer unfollow(String fromUserAlias, String toUserAlias)
			throws UserException {
		User fromUser = userDAO.findOne("profile.alias", fromUserAlias);
		if (null == fromUser) {
			throw new UserException("The user " + fromUserAlias + " not found.");
		}

		User toUser = userDAO.findOne("profile.alias", toUserAlias);
		if (null == toUser) {
			throw new UserException("The user " + toUserAlias + " not found.");
		}

		Map<String, Object> cratiaries = new HashMap<String, Object>();
		cratiaries.put("fromUser", fromUser);
		cratiaries.put("toUser", toUser);
		UserConnect userConnected = userConnectDAO.findOne(cratiaries);

		if (userConnected == null) {
			throw new UserException("The user " + fromUserAlias
					+ "hasn't followed user " + toUserAlias);
		}

		userConnectDAO.delete(userConnected);

		userDAO.decrease("profile.alias", fromUserAlias, "social.following");
		userDAO.decrease("profile.alias", toUserAlias, "social.followed");

		return (int) (toUser.getSocial().getFollowed() - 1);
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

	private List<User> getAllFollowingUsers(String fromUserAlias)
			throws UserException {
		Map<String, String> cratiaries = new HashMap<String, String>();
		cratiaries.put("fromUser.profile.alias", fromUserAlias);
		List<UserConnect> list = userConnectDAO.find(cratiaries);
		List<User> result = new ArrayList<User>();
		for (UserConnect uc : list) {
			result.add(userDAO.findOne("profile.alias", uc.getToUser()));
		}
		return result;
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

	private List<User> recommandUsersBySocial(String userAlias)
			throws UserException, SoundException {

		List<User> followingUsers = this.getAllFollowingUsers(userAlias);

		if (followingUsers.size() == 0) {
			return new ArrayList<User>();
		}

		Map<User, Long> potentialFollowing = new HashMap<User, Long>();

		// find 1-class potential follow targets
		for (User user : followingUsers) {
			List<User> firstClass = this.getAllFollowingUsers(user.getProfile()
					.getAlias());
			firstClass.removeAll(followingUsers);
			for (User firstUser : firstClass) {
				if (potentialFollowing.containsKey(firstUser)) {
					potentialFollowing.put(firstUser, 1l);
				} else {
					potentialFollowing.put(firstUser,
							potentialFollowing.get(firstUser) + 1);
				}
			}
		}

		// add weight to impress 1-class
		for (User user : potentialFollowing.keySet()) {
			potentialFollowing.put(user, potentialFollowing.get(user)
					+ SocialUtils.FIRST_CLASS_WEIGHT);
		}

		// find 2-class potential follow targets
		for (User user : potentialFollowing.keySet()) {
			List<User> secondClass = this.getAllFollowingUsers(user
					.getProfile().getAlias());
			secondClass.removeAll(followingUsers);
			secondClass.removeAll(potentialFollowing.keySet());
			for (User secondUser : secondClass) {
				if (potentialFollowing.containsKey(secondUser)) {
					potentialFollowing.put(secondUser, 1l);
				} else {
					potentialFollowing.put(secondUser,
							potentialFollowing.get(secondUser) + 1);
				}
			}
		}

		List<User> allResult = SocialUtils.toSeqList(SocialUtils
				.sortMapByValue(potentialFollowing, false));

		return allResult;
	}

	private List<User> recommandUsersByTags(Set<Tag> tags)
			throws UserException, SoundException {
		Map<Tag, List<Sound>> tagSoundMap = new HashMap<Tag, List<Sound>>();
		Map<User, Long> userTagNumMap = new HashMap<User, Long>();

		// fetch tag : sounds map
		for (Tag tag : tags) {
			tagSoundMap.put(tag, tagService.getSoundsWithTag(tag.getLabel()));
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

		Map<User, Integer> userTagSeq = SocialUtils.toSeqMap(SocialUtils
				.sortMapByValue(userTagNumMap, false));

		List<User> allResult = SocialUtils.toSeqList(SocialUtils
				.sortMapByValue(userTagSeq, false));

		return allResult;
	}

	@Override
	public List<User> recommandUsersForUser(String userAlias, Integer pageNum,
			Integer pageSize) throws UserException, SoundException {
		List<User> bySocial = recommandUsersBySocial(userAlias);

		List<Sound> liked = soundSocialService.getLikedSoundsByUser(userAlias);
		Set<Tag> tags = new HashSet<Tag>();
		for (Sound sound : liked) {
			tags.addAll(sound.getTags());
		}
		List<User> byTags = recommandUsersByTags(tags);

		List<User> candidates = combineSocialAndTagsRecommandation(bySocial,
				byTags);

		List<User> toReturn = SocialUtils.sliceList(candidates, pageNum,
				pageSize);

		if (toReturn.size() < pageNum) {
			toReturn.addAll(recommandRandomUsers(pageNum - toReturn.size()));
		}

		return toReturn;
	}

	private List<User> recommandRandomUsers(int number) {
		return userDAO.findTopOnes(number, "social.followed");
	}

	private List<User> combineSocialAndTagsRecommandation(List<User> bySocial,
			List<User> byTags) {
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
	public List<User> recommandUsersByTags(List<String> tagLabels,
			Integer pageNum, Integer pageSize) throws UserException,
			SoundException {
		Set<Tag> tags = new HashSet<Tag>();

		for (String label : tagLabels) {
			tags.add(tagService.getOrCreate(label, null));
		}

		List<User> byTags = recommandUsersByTags(tags);

		List<User> toReturn = SocialUtils.sliceList(byTags, pageNum, pageSize);

		if (toReturn.size() < pageNum) {
			toReturn.addAll(recommandRandomUsers(pageNum - toReturn.size()));
		}

		return toReturn;
	}
}

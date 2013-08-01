package com.sound.service.sound.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sound.dao.SoundDAO;
import com.sound.dao.TagDAO;
import com.sound.model.Sound;
import com.sound.model.Tag;
import com.sound.service.user.itf.UserService;

@Service
@Scope("singleton")
public class TagService implements com.sound.service.sound.itf.TagService {
	@Autowired
	SoundDAO soundDAO;

	@Autowired
	TagDAO tagDAO;

	@Autowired
	UserService userService;

	@Override
	public Tag getOrCreate(String label, String userAlias) {
		Tag tag = tagDAO.findOne("label", label);

		if (tag != null && tag.getId() != null) {
			return tag;
		}

		tag = new Tag();
		tag.setLabel(label);
		tag.setCreatedUser(userService.getUserByAlias(userAlias));
		tag.setCreatedDate(new Date());
		tagDAO.save(tag);

		return tag;
	}

	@Override
	public List<Tag> listTagsContains(String pattern) {
		return tagDAO.findByPattern("label", pattern, true);
	}

	@Override
	public List<Tag> listAll() {
		return tagDAO.find().asList();
	}

	@Override
	public void attachToSound(String soundAlias, List<String> tagLabels, String userAlias) {
		List<Tag> tags = new ArrayList<Tag>();
		for (String label : tagLabels) {
			tags.add(this.getOrCreate(label, userAlias));
		}

		Sound sound = soundDAO.findOne("profile.name", soundAlias);
		sound.addTags(tags);
		
		soundDAO.updateProperty("profile.name", soundAlias, "tags", sound.getTags());
	}

	@Override
	public void detachFromSound(String soundAlias, List<String> tagLabels, String userAlias) {
		List<Tag> tags = new ArrayList<Tag>();
		for (String label : tagLabels) {
			tags.add(this.getOrCreate(label, userAlias));
		}

		Sound sound = soundDAO.findOne("profile.name", soundAlias);
		sound.getTags().removeAll(tags);
		
		soundDAO.updateProperty("profile.name", soundAlias, "tags", sound.getTags());
		deleteOrphanTags(tags);
	}

	private void deleteOrphanTags(List<Tag> tags) {
		for (Tag tag : tags) {
			if(CollectionUtils.isEmpty(getSoundsWithTag(tag.getLabel()))) {
				tagDAO.delete(tag);
			}
		}
	}

	@Override
	public List<Sound> getSoundsWithTag(String label) {
		Tag tag = tagDAO.findOne("label", label);
		if (tag == null) {
			return new ArrayList<Sound>();
		}
		return soundDAO.fetchEntitiesPropertyContains("tags", tag);
	
	}

}

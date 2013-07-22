package com.sound.service.sound.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.sound.dao.SoundDAO;
import com.sound.dao.TagDAO;
import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.Tag;

public class TagService implements com.sound.service.sound.itf.TagService {
	@Autowired
	SoundDAO soundDAO;

	@Autowired
	TagDAO tagDAO;

	@Override
	public Tag getOrCreateTag(String label) throws SoundException {
		Tag tag = null;
		try {
			tag = tagDAO.findTagByLabel(label);
		} catch (Exception e) {
			throw new SoundException(e);
		}

		if (tag != null && tag.getId() != null) {
			return tag;
		}

		tag = new Tag();
		tag.setLabel(label);

		try {
			tagDAO.save(tag);
		} catch (Exception e) {
			throw new SoundException("Cannot create Tag with label " + label,
					e);
		}
		return tag;
	}

	@Override
	public List<Tag> listTagsContains(String pattern) throws SoundException {
		List<Tag> result = new ArrayList<Tag>();
		try {
			result.addAll(tagDAO.findTagsContains(pattern));
		} catch (Exception e) {
			throw new SoundException("Cannot find Tag containing label "
					+ pattern, e);
		}
		return result;
	}

	@Override
	public List<Tag> listAllTags() throws SoundException {
		List<Tag> result = new ArrayList<Tag>();
		try {
			result.addAll(tagDAO.findAllTags());
		} catch (Exception e) {
			throw new SoundException("Cannot find all Tags", e);
		}
		return result;
	}

	@Override
	public void attachTagsToSound(String soundId, List<String> tagLabels)
			throws SoundException {
		if (soundId == null || tagLabels == null) {
			throw new SoundException(
					"SoundId and tags should not be null when attach tags to sound");
		}

		List<Tag> tags = new ArrayList<Tag>();

		Sound sound = soundDAO.findById(soundId);
		for (String label : tagLabels) {
			tags.add(this.getOrCreateTag(label));
		}
		sound.addTags(tags);
		soundDAO.save(sound);
	}

	@Override
	public void detachTagsFromSound(String soundId, List<String> tagLabels)
			throws SoundException {
		List<Tag> tags = new ArrayList<Tag>();
		for (String label : tagLabels) {
			tags.add(this.getOrCreateTag(label));
		}

		Sound sound = soundDAO.findById(soundId);
		sound.getTags().removeAll(tags);
		soundDAO.save(sound);
	}

	@Override
	public List<Sound> getSoundsWithTag(String label) throws SoundException {
		Tag tag = this.getOrCreateTag(label);
		List<Sound> result = new ArrayList<Sound>();
		try {
			result.addAll(soundDAO.findByTagId(tag.getId()));

		} catch (Exception e) {
			throw new SoundException("Cannot find sounds by tag "
					+ tag.getLabel(), e);
		}
		return result;
	}
}

package com.sound.service.sound.itf;

import java.util.List;

import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.Tag;

public interface TagService {
	public Tag getOrCreateTag(String label) throws SoundException;

	public List<Tag> listTagsContains(String pattern) throws SoundException;

	public List<Tag> listAllTags() throws SoundException;

	public void attachTagsToSound(String soundId, List<String> tags)
			throws SoundException;

	public void detachTagsFromSound(String soundId, List<String> tags)
			throws SoundException;

	public List<Sound> getSoundsWithTag(String label) throws SoundException;
}

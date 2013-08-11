package com.sound.service.sound.itf;

import java.util.List;

import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.Tag;

public interface TagService {
	
	public Tag getOrCreate(String label, String userAlias, String tagCategory) throws SoundException;

	public List<Tag> listTagsContains(String pattern) throws SoundException;

	public List<Tag> listAll() throws SoundException;

	public void attachToSound(String soundAlias, List<String> tags, String userAlias)
			throws SoundException;

	public void detachFromSound(String soundAlias, List<String> tags, String userAlias)
			throws SoundException;

	public List<Sound> getSoundsWithTag(String label) throws SoundException;
}

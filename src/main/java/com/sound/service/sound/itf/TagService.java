package com.sound.service.sound.itf;

import java.util.List;

import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.Tag;

public interface TagService {
	
	//TODO: record created date and user.
	public Tag getOrCreate(String label) throws SoundException;

	public List<Tag> listTagsContains(String pattern) throws SoundException;

	public List<Tag> listAll() throws SoundException;

	//TODO: I guess dao.save() can't be used to update inner references.
	public void attachToSound(String soundAlias, List<String> tags)
			throws SoundException;

	//TODO: after detachment, if there is no sound attaching the tag, delete the orphan tag.
	public void detachFromSound(String soundId, List<String> tags)
			throws SoundException;

	public List<Sound> getSoundsWithTag(String label) throws SoundException;
}

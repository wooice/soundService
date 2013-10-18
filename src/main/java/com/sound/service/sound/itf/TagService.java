package com.sound.service.sound.itf;

import java.util.List;

import com.sound.exception.SoundException;
import com.sound.model.Sound;
import com.sound.model.Tag;
import com.sound.model.Tag.TagCategory;
import com.sound.model.User;

public interface TagService {

  public Tag get(Tag tag, Boolean createdOnNotFound) throws SoundException;

  public List<Tag> listTagsContains(String pattern) throws SoundException;

  public List<Tag> findCurated() throws SoundException;

  public List<TagCategory> listCategories();

  public List<Tag> attachToSound(String soundAlias, List<String> tags, User owner) throws SoundException;

  public void detachFromSound(String soundAlias, List<String> tags, User owner)
      throws SoundException;

  public List<Sound> getSoundsWithTag(String label) throws SoundException;
}

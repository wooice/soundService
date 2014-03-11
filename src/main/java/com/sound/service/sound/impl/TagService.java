package com.sound.service.sound.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sound.dao.SoundDAO;
import com.sound.dao.TagCategoryDAO;
import com.sound.dao.TagDAO;
import com.sound.model.Sound;
import com.sound.model.Tag;
import com.sound.model.Tag.TagCategory;
import com.sound.model.User;
import com.sound.service.user.itf.UserService;

@Service
@Scope("singleton")
public class TagService implements com.sound.service.sound.itf.TagService {
  @Autowired
  SoundDAO soundDAO;

  @Autowired
  TagDAO tagDAO;

  @Autowired
  TagCategoryDAO tagCategoryDAO;

  @Autowired
  UserService userService;

  @Override
  public Tag get(Tag input, Boolean createdOnNotFound) {
    Tag tag = tagDAO.findOne("label", input.getLabel());

    if (tag != null && tag.getId() != null) {
      return tag;
    }
 
    if (!createdOnNotFound)
    {
      return null;
    }

    TagCategory category = null;

    if (null != input.getCategory() && null != input.getCategory().getName())
    {
      category = tagCategoryDAO.findOne("name", input.getCategory().getName());
      
      if (null == category)
      {
    	input.getCategory().setCreatedTime(new Date());
    	input.getCategory().setCurated(input.isCurated());
        tagCategoryDAO.save(input.getCategory());
      }
      else
      {
    	 input.setCategory(category);
      }
    }

    tagDAO.save(input);

    return input;
  }

  @Override
  public List<Tag> listTagsContains(String pattern) {
    return tagDAO.findByPattern("label", pattern, true);
  }

  @Override
  public List<Tag> findCurated() {
    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("curated", true);

    return tagDAO.find(cratiaries);
  }

  @Override
  public List<Tag> attachToSound(String id, List<String> tagLabels, User owner) {
    List<Tag> tags = new ArrayList<Tag>();
    for (String label : tagLabels) {
      Tag tag = new Tag();
      tag.setLabel(label);
      tag.setCurated(false);
      tags.add(this.get(tag, true));
    }

    Sound sound = soundDAO.findOne("_id", new ObjectId(id));
    sound.addTags(tags);

    soundDAO.updateProperty("_id", new ObjectId(id), "tags", sound.getTags());

    return tags;
  }

  @Override
  public void detachFromSound(String id, List<String> tagLabels, User owner) {
    List<Tag> tags = new ArrayList<Tag>();
    for (String label : tagLabels) {
      Tag tag = new Tag();
      tag.setLabel(label);
      tag.setCurated(false);
      tags.add(this.get(tag, true));
    }

    Sound sound = soundDAO.findOne("_id", new ObjectId(id));
    sound.getTags().removeAll(tags);

    soundDAO.updateProperty("_id", sound.getId(), "tags", sound.getTags());
//    deleteOrphanTags(tags);
  }

  protected void deleteOrphanTags(List<Tag> tags) {
    for (Tag tag : tags) {
      if (CollectionUtils.isEmpty(getSoundsWithTag(tag.getLabel()))) {
        tagDAO.delete(tag);
      }
    }
  }

  @Override
  public List<Sound> getSoundsWithTag(String label) {
    if (null == label)
    {
      return Collections.emptyList();
    }
    Tag tag = tagDAO.findOne("label", label);
    if (tag == null) {
      return Collections.emptyList();
    }
    return soundDAO.fetchEntitiesPropertyContains("tags", tag);
  }

  @Override
  public List<TagCategory> listCategories() {
    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("curated", true);

    return tagCategoryDAO.find(cratiaries);
  }

}

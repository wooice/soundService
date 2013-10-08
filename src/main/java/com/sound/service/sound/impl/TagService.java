package com.sound.service.sound.impl;

import java.util.ArrayList;
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
  public Tag getOrCreate(String label, User owner, String tagCategory) {
    Tag tag = tagDAO.findOne("label", label);

    if (tag != null && tag.getId() != null) {
      return tag;
    }

    TagCategory category = tagCategoryDAO.findOne("name", tagCategory);
    tag = new Tag();
    tag.setLabel(label);
    tag.setCreatedUser(owner);
    tag.setCategory(category);
    tag.setCreatedDate(new Date());
    tagDAO.save(tag);

    return tag;
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
  public void attachToSound(String id, List<String> tagLabels, User owner) {
    List<Tag> tags = new ArrayList<Tag>();
    for (String label : tagLabels) {
      tags.add(this.getOrCreate(label, owner, null));
    }

    Sound sound = soundDAO.findOne("_id", new ObjectId(id));
    sound.addTags(tags);

    soundDAO.updateProperty("_id", new ObjectId(id), "tags", sound.getTags());
  }

  @Override
  public void detachFromSound(String id, List<String> tagLabels, User owner) {
    List<Tag> tags = new ArrayList<Tag>();
    for (String label : tagLabels) {
      tags.add(this.getOrCreate(label, owner, null));
    }

    Sound sound = soundDAO.findOne("_id", new ObjectId(id));
    sound.getTags().removeAll(tags);

    soundDAO.updateProperty("_id", sound.getId(), "tags", sound.getTags());
    deleteOrphanTags(tags);
  }

  private void deleteOrphanTags(List<Tag> tags) {
    for (Tag tag : tags) {
      if (CollectionUtils.isEmpty(getSoundsWithTag(tag.getLabel()))) {
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

  @Override
  public List<TagCategory> listCategories() {
    Map<String, Object> cratiaries = new HashMap<String, Object>();
    cratiaries.put("curated", true);

    return tagCategoryDAO.find(cratiaries);
  }

}

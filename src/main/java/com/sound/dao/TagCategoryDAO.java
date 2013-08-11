package com.sound.dao;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.mongodb.Mongo;
import com.sound.model.Tag.TagCategory;
import com.sound.morphia.extension.BaseDAO;

public class TagCategoryDAO extends BaseDAO<TagCategory, ObjectId> {

  public TagCategoryDAO(Mongo mongo, Morphia morphia, String dbName) {
    super(mongo, morphia, dbName);
  }
}

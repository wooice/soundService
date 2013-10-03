package com.sound.dao;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.mongodb.Mongo;
import com.sound.model.Tag;
import com.sound.morphia.extension.BaseDAO;

public class TagDAO extends BaseDAO<Tag, ObjectId> {

  public TagDAO(Mongo mongo, Morphia morphia, String dbName) {
    super(mongo, morphia, dbName);
  }

}

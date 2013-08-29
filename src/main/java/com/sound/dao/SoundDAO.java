package com.sound.dao;

import java.util.List;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.github.jmkgreen.morphia.query.Query;
import com.mongodb.Mongo;
import com.sound.model.Sound;
import com.sound.morphia.extension.BaseDAO;

public class SoundDAO extends BaseDAO<Sound, ObjectId> {

  public SoundDAO(Mongo mongo, Morphia morphia, String dbName) {
    super(mongo, morphia, dbName);
  }

  public List<Sound> findByKeyWord(String keyWord, Integer start, Integer range) {
    Query<Sound> query = createQuery();

    query.or(query.criteria("profile.name").containsIgnoreCase(keyWord)).or(
        query.criteria("profile.description").containsIgnoreCase(keyWord));

    query.offset(start).limit(range).order("-profile。createdTime");

    return this.find(query).asList();
  }

}

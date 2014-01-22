package com.sound.dao;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.mongodb.Mongo;
import com.sound.model.User.PlayRecord;
import com.sound.morphia.extension.BaseDAO;

public class PlayRecordDAO extends BaseDAO<PlayRecord, ObjectId> {

  public PlayRecordDAO(Mongo mongo, Morphia morphia, String dbName) {
    super(mongo, morphia, dbName);
  }
}
package com.sound.dao;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.mongodb.Mongo;
import com.sound.model.UserAuth;
import com.sound.morphia.extension.BaseDAO;

public class UserAuthDAO extends BaseDAO<UserAuth, ObjectId> {
  public UserAuthDAO(Mongo mongo, Morphia morphia, String dbName) {
    super(mongo, morphia, dbName);
  }

}

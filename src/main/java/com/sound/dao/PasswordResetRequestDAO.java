package com.sound.dao;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.mongodb.Mongo;
import com.sound.model.UserAuth.PasswordResetRequest;
import com.sound.morphia.extension.BaseDAO;

public class PasswordResetRequestDAO extends BaseDAO<PasswordResetRequest, ObjectId>{

  public PasswordResetRequestDAO(Mongo mongo, Morphia morphia, String dbName) {
    super(mongo, morphia, dbName);
  }

}

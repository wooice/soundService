package com.sound.dao;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.mongodb.Mongo;
import com.sound.model.UserMessage;
import com.sound.morphia.extension.BaseDAO;

public class UserMessageDAO extends BaseDAO<UserMessage, ObjectId>{

  public UserMessageDAO(Mongo mongo, Morphia morphia,
          String dbName) 
  {
      super(mongo, morphia, dbName);
  }

}

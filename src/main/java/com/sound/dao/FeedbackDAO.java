package com.sound.dao;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.mongodb.Mongo;
import com.sound.model.Feedback;
import com.sound.morphia.extension.BaseDAO;

public class FeedbackDAO extends BaseDAO<Feedback, ObjectId> {

  public FeedbackDAO(Mongo mongo, Morphia morphia, String dbName) {
    super(mongo, morphia, dbName);
  }
}

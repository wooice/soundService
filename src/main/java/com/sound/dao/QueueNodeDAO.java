package com.sound.dao;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.mongodb.Mongo;
import com.sound.model.Sound.QueueNode;
import com.sound.morphia.extension.BaseDAO;

public class QueueNodeDAO extends BaseDAO<QueueNode, ObjectId>{

  public QueueNodeDAO(Mongo mongo, Morphia morphia,
          String dbName) 
  {
      super(mongo, morphia, dbName);
  }
}
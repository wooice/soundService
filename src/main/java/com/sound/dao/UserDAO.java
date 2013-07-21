package com.sound.dao;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.mongodb.Mongo;
import com.sound.model.User;

public class UserDAO extends BaseDAO<User, ObjectId>{

	protected UserDAO(Mongo mongo, Morphia morphia, String dbName) 
	{
		super(mongo, morphia, dbName);
	}

}

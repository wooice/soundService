package com.sound.dao;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.mongodb.Mongo;
import com.sound.model.UserActivity.UserConnect;
import com.sound.morphia.extension.BaseDAO;

public class UserConnectDAO  extends BaseDAO<UserConnect, ObjectId>{

	public UserConnectDAO(Mongo mongo, Morphia morphia,
			String dbName) 
	{
		super(mongo, morphia, dbName);
	}

}

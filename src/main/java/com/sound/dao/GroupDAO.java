package com.sound.dao;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.mongodb.Mongo;
import com.sound.model.Group;
import com.sound.morphia.extension.BaseDAO;

public class GroupDAO extends BaseDAO<Group, ObjectId>{

	public GroupDAO(Mongo mongo, Morphia morphia,
			String dbName) 
	{
		super(mongo, morphia, dbName);
	}
}

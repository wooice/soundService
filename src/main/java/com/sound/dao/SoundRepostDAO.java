package com.sound.dao;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.mongodb.Mongo;
import com.sound.model.SoundActivity.SoundRepost;
import com.sound.morphia.extension.BaseDAO;

public class SoundRepostDAO extends BaseDAO<SoundRepost, ObjectId>{

	public SoundRepostDAO(Mongo mongo, Morphia morphia,
			String dbName) 
	{
		super(mongo, morphia, dbName);
	}
}

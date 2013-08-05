package com.sound.dao;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.mongodb.Mongo;
import com.sound.model.SoundActivity.SoundPlay;
import com.sound.morphia.extension.BaseDAO;

public class SoundPlayDAO extends BaseDAO<SoundPlay, ObjectId>{

	public SoundPlayDAO(Mongo mongo, Morphia morphia,
			String dbName) 
	{
		super(mongo, morphia, dbName);
	}

}

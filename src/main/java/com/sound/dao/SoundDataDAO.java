package com.sound.dao;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.mongodb.Mongo;
import com.sound.model.Sound.SoundData;

public class SoundDataDAO  extends BaseDAO<SoundData, ObjectId>{

	protected SoundDataDAO(Mongo mongo, Morphia morphia, String dbName) 
	{
		super(mongo, morphia, dbName);
	}

}

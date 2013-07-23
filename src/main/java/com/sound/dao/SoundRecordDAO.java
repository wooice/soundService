package com.sound.dao;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.mongodb.Mongo;
import com.sound.model.SoundActivity.SoundRecord;
import com.sound.morphia.extension.BaseDAO;

public class SoundRecordDAO extends BaseDAO<SoundRecord, ObjectId>{

	public SoundRecordDAO(Mongo mongo, Morphia morphia,
			String dbName) 
	{
		super(mongo, morphia, dbName);
	}
}

package com.sound.dao;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.github.jmkgreen.morphia.dao.BasicDAO;
import com.mongodb.Mongo;
import com.sound.model.Sound;

public class SoundDAO extends BasicDAO<Sound, ObjectId>{

	public SoundDAO(Mongo mongo, Morphia morphia,
			String dbName) {
		super(mongo, morphia, dbName);
	}
	
}

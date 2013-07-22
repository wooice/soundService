package com.sound.dao;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.mongodb.Mongo;
import com.sound.model.SoundActivity.SoundLike;
import com.sound.morphia.extension.BaseDAO;

public class SoundLikeDAO extends BaseDAO<SoundLike, ObjectId>{

	public SoundLikeDAO(Mongo mongo, Morphia morphia,
			String dbName) 
	{
		super(mongo, morphia, dbName);
	}

}

package com.sound.dao;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.mongodb.Mongo;
import com.sound.model.SoundActivity.SoundComment;
import com.sound.morphia.extension.BaseDAO;

public class SoundCommentDAO extends BaseDAO<SoundComment, ObjectId>{

	public SoundCommentDAO(Mongo mongo, Morphia morphia,
			String dbName) 
	{
		super(mongo, morphia, dbName);
	}

}
package com.sound.dao;

import java.util.Collection;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.github.jmkgreen.morphia.query.Query;
import com.mongodb.Mongo;
import com.sound.model.Sound;

public class SoundDAO extends BaseDAO<Sound, ObjectId>{

	public SoundDAO(Mongo mongo, Morphia morphia,
			String dbName) 
	{
		super(mongo, morphia, dbName);
	}
	
	public Sound findById(String soundId) 
	{
		Query<Sound> query = this.ds.createQuery(Sound.class);
		query.filter("id", soundId);
		return this.findOne(query);
	}
	
	public Collection<? extends Sound> findByTagId(ObjectId id) 
	{
		Query<Sound> query = this.ds.createQuery(Sound.class);
		query.field("tags").hasThisOne(id);
		return this.find(query).asList();
	}
}

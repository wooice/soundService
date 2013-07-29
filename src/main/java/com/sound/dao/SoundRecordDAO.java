package com.sound.dao;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.github.jmkgreen.morphia.query.Query;
import com.mongodb.Mongo;
import com.sound.model.SoundActivity.SoundRecord;
import com.sound.model.User;
import com.sound.morphia.extension.BaseDAO;

public class SoundRecordDAO extends BaseDAO<SoundRecord, ObjectId>{

	public SoundRecordDAO(Mongo mongo, Morphia morphia,
			String dbName) 
	{
		super(mongo, morphia, dbName);
	}
	
	public List<SoundRecord> findByOwners(Map<String, String> cratiaries, List<User> users, Integer start, Integer range)
	{
		Query<SoundRecord> query = createQuery();
		
		for(String key: cratiaries.keySet())
		{
			query.field(key).equal(cratiaries.get(key));
		}
		
		query.field("owner").in(users);
		query.offset(start).limit(range);
		
		return this.find(query).asList();
	}
}

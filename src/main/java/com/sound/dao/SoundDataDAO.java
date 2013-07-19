package com.sound.dao;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.github.jmkgreen.morphia.dao.BasicDAO;
import com.github.jmkgreen.morphia.query.Query;
import com.mongodb.Mongo;
import com.sound.model.Sound.SoundData;

public class SoundDataDAO  extends BasicDAO<SoundData, ObjectId>{

	protected SoundDataDAO(Mongo mongo, Morphia morphia, String dbName) {
		super(mongo, morphia, dbName);
	}

	public SoundData findOneByRemoteId(String remoteId) {
		Query<SoundData> query = this.ds.createQuery(SoundData.class);
		query.filter("objectId", remoteId);
		return this.findOne(query);
	}

}

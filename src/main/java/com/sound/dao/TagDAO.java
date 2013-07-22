package com.sound.dao;

import java.util.List;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.github.jmkgreen.morphia.query.Query;
import com.mongodb.Mongo;
import com.sound.model.Tag;
import com.sound.morphia.extension.BaseDAO;

public class TagDAO extends BaseDAO<Tag, ObjectId> {

	public TagDAO(Mongo mongo, Morphia morphia, String dbName) {
		super(mongo, morphia, dbName);
	}

	public List<Tag> findAllTags() {
		Query<Tag> query = this.ds.createQuery(Tag.class);
		return query.asList();
	}

}
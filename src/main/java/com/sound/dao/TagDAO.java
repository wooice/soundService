package com.sound.dao;

import java.util.List;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.github.jmkgreen.morphia.dao.BasicDAO;
import com.github.jmkgreen.morphia.query.Query;
import com.mongodb.Mongo;
import com.sound.model.Tag;

public class TagDAO extends BasicDAO<Tag, ObjectId> {

	public TagDAO(Mongo mongo, Morphia morphia, String dbName) {
		super(mongo, morphia, dbName);
	}

	public Tag findTagByLabel(String label) {
		Query<Tag> query = this.ds.createQuery(Tag.class);
		query.field("label").equal(label);
		return this.findOne(query);
	}

	public List<Tag> findAllTags() {
		Query<Tag> query = this.ds.createQuery(Tag.class);
		return query.asList();
	}

	public List<Tag> findTagsContains(String pattern) {
		Query<Tag> query = this.ds.createQuery(Tag.class);
		query.field("label").containsIgnoreCase(pattern);
		return query.asList();
	}

}
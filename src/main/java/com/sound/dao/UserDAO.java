package com.sound.dao;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.github.jmkgreen.morphia.dao.BasicDAO;
import com.github.jmkgreen.morphia.query.Query;
import com.mongodb.Mongo;
import com.sound.model.User;

public class UserDAO extends BasicDAO<User, ObjectId>{

	protected UserDAO(Mongo mongo, Morphia morphia, String dbName) {
		super(mongo, morphia, dbName);
	}

	public User findByAlias(String alias)
	{
		Query<User> query = this.ds.createQuery(User.class);
		query.filter("profile.alias", alias);
		return this.findOne(query);
	}

	public User findByEmail(String email)
	{
		Query<User> query = this.ds.createQuery(User.class);
		query.filter("emails.emailAddress", email);
		return this.findOne(query);
	}

}

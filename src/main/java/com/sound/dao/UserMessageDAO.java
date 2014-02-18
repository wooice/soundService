package com.sound.dao;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.Morphia;
import com.github.jmkgreen.morphia.query.Query;
import com.mongodb.Mongo;
import com.sound.model.User;
import com.sound.model.UserMessage;
import com.sound.morphia.extension.BaseDAO;

public class UserMessageDAO extends BaseDAO<UserMessage, ObjectId> {

  public UserMessageDAO(Mongo mongo, Morphia morphia, String dbName) {
    super(mongo, morphia, dbName);
  }

  public List<UserMessage> findMessageList(User curUser, Integer start, Integer range, String order) {
    List<String> statuses = new ArrayList<String>();
    statuses.add("read");
    statuses.add("unread");
    Query<UserMessage> query = createQuery();
    query.or(query.and(query.criteria("from").equal(curUser),
        query.criteria("fromStatus").equal("read")), query.and(query.criteria("to").equal(curUser),
        query.criteria("toStatus").hasAnyOf(statuses)));

    query.order(order).offset(start).limit(range);

    return this.find(query).asList();
  }

  public long countMessageList(User curUser) {
    List<String> statuses = new ArrayList<String>();
    statuses.add("read");
    statuses.add("unread");
    Query<UserMessage> query = createQuery();
    query.or(query.and(query.criteria("from").equal(curUser),
        query.criteria("fromStatus").hasAnyOf(statuses)), query.and(query.criteria("to").equal(curUser),
        query.criteria("toStatus").hasAnyOf(statuses)));

    return this.count(query);
  }
  
  public long countUnreadMessage(User curUser) {
    Query<UserMessage> query = createQuery();
    query.or(query.and(query.criteria("from").equal(curUser),
        query.criteria("toStatus").equal("unread")), query.and(query.criteria("to").equal(curUser),
        query.criteria("toStatus").equal("unread")));

    return this.count(query);
  }
}

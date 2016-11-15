package com.sound.morphia.extension;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.jmkgreen.morphia.Morphia;
import com.github.jmkgreen.morphia.dao.BasicDAO;
import com.github.jmkgreen.morphia.query.Query;
import com.github.jmkgreen.morphia.query.UpdateOperations;
import com.mongodb.Mongo;

public class BaseDAO<T, PK> extends BasicDAO<T, PK> {

  private Class<T> clazz;

  @SuppressWarnings("unchecked")
  public BaseDAO(Mongo mongo, Morphia morphia, String dbName) {
    super(mongo, morphia, dbName);

    this.clazz =
        (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
  }

  public void deleteByProperty(String key, Object value) {
    Query<T> q = ds.find(clazz, key, value);
    this.deleteByQuery(q);
  }
  
  public void deleteByProperties(Map<String, Object> cratiaries) {
    Query<T> query = this.ds.createQuery(clazz);

    for (String key : cratiaries.keySet()) {
      query.field(key).equal(cratiaries.get(key));
    }

    this.deleteByQuery(query);
  }

  public List<T> find(String key, Object value) {
    Query<T> query = this.ds.createQuery(clazz);
    query.field(key).equal(value);

    return this.find(query).asList();
  }

  public List<T> find(Map<String, Object> cratiaries) {
    Query<T> query = this.ds.createQuery(clazz);

    for (String key : cratiaries.keySet()) {
      query.field(key).equal(cratiaries.get(key));
    }

    return this.find(query).asList();
  }

  public T findOne(Map<String, Object> cratiaries) {
    Query<T> query = ds.createQuery(clazz);

    for (String key : cratiaries.keySet()) {
      query.field(key).equal(cratiaries.get(key));
    }

    return this.findOne(query);
  }

  public List<T> findByPattern(String property, String pattern, boolean ignoreCase) {
    Query<T> query = this.ds.createQuery(this.clazz);

    if (ignoreCase) {
      query.field(property).containsIgnoreCase(pattern);
    } else {
      query.field(property).contains(pattern);
    }

    return this.find(query).asList();
  }
  
  public long count(Map<String, Object> cratiaries) {
    Query<T> query = this.ds.createQuery(clazz);

    for (String key : cratiaries.keySet()) {
      query.field(key).equal(cratiaries.get(key));
    }

    return this.count(query);
  }

  public void increase(String key, Object value, String property) {
    Query<T> updateQuery = ds.createQuery(this.clazz).field(key).equal(value);
    UpdateOperations<T> ops = ds.createUpdateOperations(this.clazz).inc(property);
    this.update(updateQuery, ops);
  }

  public void decrease(String key, Object value, String property) {
    Query<T> updateQuery = ds.createQuery(this.clazz).field(key).equal(value);
    UpdateOperations<T> ops = ds.createUpdateOperations(this.clazz).dec(property);
    this.update(updateQuery, ops);
  }

  public void updateProperty(String key, Object value, String property, Object object) {
    Query<T> updateQuery = ds.createQuery(this.clazz).field(key).equal(value);
    UpdateOperations<T> ops = ds.createUpdateOperations(this.clazz).set(property, object);
    this.update(updateQuery, ops);
  }

  public void updateProperty(Map<String, Object> cratiaries, String property, Object object) {
	    Query<T> updateQuery = this.ds.createQuery(clazz);

	    for (String key : cratiaries.keySet()) {
	    	updateQuery.field(key).equal(cratiaries.get(key));
	    }
	    UpdateOperations<T> ops = ds.createUpdateOperations(this.clazz).set(property, object);
	    this.update(updateQuery, ops);
  }
  
  public List<T> fetchEntitiesPropertyContains(String property, Object target) {
    List<T> result = new ArrayList<T>();
    Query<T> query = createQuery().field(property).hasThisOne(target);
    for (T t : query.fetch()) {
      result.add(t);
    }
    return result;
  }

  public List<T> findWithRange(Map<String, Object> cratiaries, Integer start, Integer range,
      String order) {
    Query<T> query = ds.createQuery(clazz);

    for (String key : cratiaries.keySet()) {
      if (cratiaries.get(key) instanceof Iterable) {
        query.field(key).hasAnyOf((Iterable<?>) cratiaries.get(key));
      } else {
        query.field(key).equal(cratiaries.get(key));
      }
    }
    query.order(order).offset(start).limit(range);

    return this.find(query).asList();
  }

  public List<T> findTopOnes(Integer number, Map<String, List<Object>> exclude, String order) {
    Query<T> query = ds.createQuery(clazz);

    for (String key : exclude.keySet()) {
      query.field(key).hasNoneOf(exclude.get(key));
    }

    query.order(order).offset(0).limit(number);

    return this.find(query).asList();
  }

}

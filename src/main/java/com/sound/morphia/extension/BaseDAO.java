package com.sound.morphia.extension;

import java.lang.reflect.ParameterizedType;
import java.util.Map;

import com.github.jmkgreen.morphia.Morphia;
import com.github.jmkgreen.morphia.dao.BasicDAO;
import com.github.jmkgreen.morphia.query.Query;
import com.github.jmkgreen.morphia.query.UpdateOperations;
import com.mongodb.Mongo;

public class BaseDAO<T, PK>  extends BasicDAO<T, PK>{

	private Class<T> clazz;  
	
	@SuppressWarnings("unchecked")  
	public BaseDAO(Mongo mongo, Morphia morphia,
			String dbName) {
		super(mongo, morphia, dbName);
		
		this.clazz = (Class<T>) ((ParameterizedType) getClass()  
	                .getGenericSuperclass()).getActualTypeArguments()[0];  
	}
	
	public void deleteByProperty(String key, String value)
	{
		Query<T> q = ds.find(clazz, key, value);
		this.deleteByQuery(q);
	}
	
	public T findOne(Map<String, String> cratiaries)
	{
		Query<T> query = ds.createQuery(clazz);
		
		for(String key: cratiaries.keySet())
		{
			query.field(key).equal(cratiaries.get(key));
		}
		
		return this.findOne(query);
	}
	
	public void increase(String key, String value, String property)
	{
		Query<T> updateQuery = ds.createQuery(this.clazz).field(key).equal(value);
		UpdateOperations<T> ops = ds.createUpdateOperations(this.clazz).inc(property);
		this.update(updateQuery, ops); 
	}

	public void decrease(String key, String value, String property)
	{
		Query<T> updateQuery = ds.createQuery(this.clazz).field(key).equal(value);
		UpdateOperations<T> ops = ds.createUpdateOperations(this.clazz).dec(property);
		this.update(updateQuery, ops); 
	}
}

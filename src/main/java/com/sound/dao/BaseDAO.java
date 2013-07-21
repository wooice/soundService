package com.sound.dao;

import java.lang.reflect.ParameterizedType;

import com.github.jmkgreen.morphia.Morphia;
import com.github.jmkgreen.morphia.dao.BasicDAO;
import com.github.jmkgreen.morphia.query.Query;
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
}

package com.sound.model;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.annotations.Id;

public class Group {

	@Id private ObjectId id;

	private String name;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}

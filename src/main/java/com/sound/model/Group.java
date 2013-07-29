package com.sound.model;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;

@Entity(noClassnameStored= true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group extends BaseModel{

	@Id private ObjectId id;

	private String name;
	
	private String description;
	
	private User owner;
	
	private List<User> admins;

	@JsonIgnore
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public List<User> getAdmins() {
		return admins;
	}

	public void setAdmins(List<User> admins) {
		this.admins = admins;
	}
	
	public void addAdmin(User user)
	{
		this.admins = (null == this.admins)? new ArrayList<User>() : this.admins;
		
		for(User oneUser: this.admins)
		{
			if (oneUser == user)
			{
				return;
			}
		}
		this.admins.add(user);
	}
	
	public void removeAdmin(User user)
	{
		this.admins = (null == this.admins)? new ArrayList<User>() : this.admins;
		this.admins.remove(user);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Group other = (Group) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}

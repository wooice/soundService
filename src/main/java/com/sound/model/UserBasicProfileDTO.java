package com.sound.model;

import java.util.List;

public class UserBasicProfileDTO {
	private String alias;
	private String avatorUrl;
	private String firstname;
	private String lastname;
	private String city;
	private String country;
	private String description;
	private List<Integer> occupations;
	
	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}


	public String getAvatorUrl() {
		return avatorUrl;
	}

	public void setAvatorUrl(String avatorUrl) {
		this.avatorUrl = avatorUrl;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Integer> getOccupations() {
		return occupations;
	}

	public void setOccupations(List<Integer> occupations) {
		this.occupations = occupations;
	}

}

package com.sound.model;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.Id;

public class UserAuth {

	@Id private ObjectId id;

	private String password;

	@Embedded
	private List<ChangeHistory> hisoties;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	public List<ChangeHistory> getHisoties() {
		return hisoties;
	}


	public void setHisoties(List<ChangeHistory> hisoties) {
		this.hisoties = hisoties;
	}


	public static class ChangeHistory
	{
		private String ip;
		private String password;
		private Date modifiedDate;
		public String getIp() {
			return ip;
		}
		public void setIp(String ip) {
			this.ip = ip;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		public Date getModifiedDate() {
			return modifiedDate;
		}
		public void setModifiedDate(Date modifiedDate) {
			this.modifiedDate = modifiedDate;
		}
	}

}

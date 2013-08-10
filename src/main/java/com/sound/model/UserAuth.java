package com.sound.model;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;

@Entity(noClassnameStored= true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserAuth extends BaseModel
{

	@Id private ObjectId id;

	private String password;

	@Embedded
	private List<ChangeHistory> histories;

	@JsonIgnore
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


	public List<ChangeHistory> getHistories() {
		return histories;
	}


	public void setHistories(List<ChangeHistory> histories) {
		this.histories = histories;
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

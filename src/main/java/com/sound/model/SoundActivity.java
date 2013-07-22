package com.sound.model;

import java.util.Date;
import java.util.List;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.Reference;

public class SoundActivity {

	@Reference
	private Sound sound;
	
	@Reference
	private User owner;

	private Date createdTime;
	
	public Sound getSound() {
		return sound;
	}

	public void setSound(Sound sound) {
		this.sound = sound;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}
	
	public static class SoundComment extends SoundActivity
	{
		private String comment;

		private float pointAt;

		@Embedded
		private List<SoundCommentReply> replies;
		
		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}
 
		public float getPointAt() {
			return pointAt;
		}

		public void setPointAt(float pointAt) {
			this.pointAt = pointAt;
		}

		public List<SoundCommentReply> getReplies() {
			return replies;
		}


		public void setReplies(List<SoundCommentReply> replies) {
			this.replies = replies;
		}

		public class SoundCommentReply extends SoundActivity
		{
			private String reply;

			public String getReply() {
				return reply;
			}

			public void setReply(String reply) {
				this.reply = reply;
			}
		}
	}

	public static class SoundLike extends SoundActivity
	{
	}

	public static class SoundRepost extends SoundActivity
	{
	}

	public static class SoundShare extends SoundActivity
	{
	}
}

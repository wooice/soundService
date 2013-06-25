package com.sound.model;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.Reference;

public class SoundSocial
{
	@Id private ObjectId id;

	@Embedded
	private List<SoundLike> likes;

	@Embedded
	private List<SoundRepost> reposts;

	@Embedded
	private List<SoundComment> comments;

	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public List<SoundComment> getComments() {
		return comments;
	}

	public void setComments(List<SoundComment> comments) {
		this.comments = comments;
	}

	public List<SoundLike> getLikes() {
		return likes;
	}

	public void setLikes(List<SoundLike> likes) {
		this.likes = likes;
	}

	public List<SoundRepost> getReposts() {
		return reposts;
	}

	public void setReposts(List<SoundRepost> reposts) {
		this.reposts = reposts;
	}

	public static class SoundActivity
	{
		@Reference(lazy=true)
		private User owner;

		private Date createdTime;

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
	}

	public static class SoundComment extends SoundActivity
	{
		private String comment;

		private long pointAt;

		private List<SoundCommentReply> replies;
		
		public String getComment() {
			return comment;
		}


		public void setComment(String comment) {
			this.comment = comment;
		}


		public long getPointAt() {
			return pointAt;
		}


		public void setPointAt(long pointAt) {
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

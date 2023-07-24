package org.unicefkidpower.kid_power.Model.Structure;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by Ruifeng Shi on 12/5/2015.
 */
public class KPHMessage {
	public static final String TYPE_CHEER = "cheer";
	public static final String TYPE_MESSAGE = "message";

	@SerializedName("_id")
	private int _id;

	@SerializedName("senderId")
	private int senderId;

	@SerializedName("senderName")
	private String senderName;

	@SerializedName("recipientId")
	private int recipientId;

	@SerializedName("type")
	private String type;

	@SerializedName("content")
	private MessageContent content;

	@SerializedName("minimumApiVersion")
	private String minimumApiVersion;

	@SerializedName("updatedAt")
	private Date updatedAt;

	@SerializedName("createdAt")
	private Date createdAt;

	public KPHMessage(
			int id,
			int senderId,
			String senderName,
			int recipientId,
			String type,
			MessageContent content,
			String minimumApiVersion,
			Date updatedAt,
			Date createdAt
	) {
		this._id = id;
		this.senderId = senderId;
		this.senderName = senderName;
		this.recipientId = recipientId;
		this.type = type;
		this.content = content;
		this.minimumApiVersion = minimumApiVersion;
		this.updatedAt = updatedAt;
		this.createdAt = createdAt;
	}

	public int getId() {
		return _id;
	}

	public void setId(int id) {
		this._id = id;
	}

	public int getSenderId() {
		return senderId;
	}

	public void setSenderId(int id) {
		this.senderId = senderId;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public int getRecipientId() {
		return recipientId;
	}

	public void setRecipientId(int recipientId) {
		this.recipientId = recipientId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public MessageContent getContent() {
		return content;
	}

	public void setContent(MessageContent content) {
		this.content = content;
	}

	public String getMinimumApiVersion() {
		return minimumApiVersion;
	}

	public void setMinimumApiVersion(String minimumApiVersion) {
		this.minimumApiVersion = minimumApiVersion;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public class MessageContent {

		@SerializedName("cheerId")
		String cheerId;

		public MessageContent(
				String cheerId
		) {
			this.cheerId = cheerId;
		}

		public String getCheerId() {
			return cheerId;
		}

		public void setCheerId(String cheerId) {
			this.cheerId = cheerId;
		}
	}
}

package com.zerobudget.bookito.models.chat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MessageModel {
    private String sender;
    private String receiver;
    private String message; //contenuto del messaggio
    private String status; //sent, read
    private String messageId;
    private long messageSentAt;//timestamp in secondi

    public MessageModel() {
    }

    public MessageModel(String sender, String receiver, String message, String status, long messageSentAt) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.status = status;
        this.messageSentAt = messageSentAt;
    }

    public MessageModel(String sender, String receiver, String message, String status, long messageSentAt, String id) {
        this(sender, receiver, message, status, messageSentAt);
        this.messageId = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public static ArrayList<MessageModel> getMessages(String user1, String user2, String requestID) {
        //TODO get un array of message between two users (maybe could be useful?)
        return null;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getMessageSentAt() {
        return messageSentAt;
    }

    public void setMessageSentAt(long messageSentAt) {
        this.messageSentAt = messageSentAt;
    }

    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", this.message);
        map.put("mmessageSentAt", this.messageSentAt);
        map.put("receiver", this.receiver);
        map.put("sender", this.sender);
        map.put("status", this.status);

        return map;
    }
}

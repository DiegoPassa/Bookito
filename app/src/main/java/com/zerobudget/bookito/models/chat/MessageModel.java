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
    private String messageTime; //orario d'inivio
    private String messageDate; //data d'invio
    //private Timestamp messageSentAt;

    public MessageModel() {
    }

    public MessageModel(String sender, String receiver, String message, String status, String messageTime, String messageDate) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.status = status;
        this.messageTime = messageTime;
        this.messageDate = messageDate;
    }

    public MessageModel(String sender, String receiver, String message, String status, String messageTime, String messageDate, String id) {
        this(sender, receiver, message, status, messageTime, messageDate);
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

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }


    public String getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(String messageDate) {
        this.messageDate = messageDate;
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


    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", this.message);
        map.put("messageDate", this.messageDate);
        map.put("messageTime", this.messageTime);
        map.put("receiver", this.receiver);
        map.put("sender", this.sender);
        map.put("status", this.status);

        return map;
    }
}

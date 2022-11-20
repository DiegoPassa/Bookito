package com.zerobudget.bookito.models.Chat;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class MessageModel {
    private String sender;
    private String receiver;
    private String message;
    private String messageId;
    private Timestamp messageSentAt;

    public MessageModel(){}

    public MessageModel(String sender, String receiver, String message, Timestamp timeStamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.messageSentAt = timeStamp;
    }

    public MessageModel(String sender, String receiver, String message, Timestamp timeStamp, String id) {
       this(sender, receiver, message, timeStamp);
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

    public Timestamp getMessageSentAt() {
        return messageSentAt;
    }

    public void setMessageSentAt(Timestamp messageSentAt) {
        this.messageSentAt = messageSentAt;
    }

    public static ArrayList<MessageModel> getMessages(String user1, String user2, String requestID) {
        //TODO get un array of message between two users (maybe could be useful?)
        return null;
    }
}

package com.zerobudget.bookito.models.Chat;

import java.util.ArrayList;

public class MessageModel {
    private String sender;
    private String receiver;
    private String message;
    private String messageId;
    private String messageTime;
    private String messageDate;
    //private Timestamp messageSentAt;

    public MessageModel(){}

    public MessageModel(String sender, String receiver, String message, String messageTime, String messageDate) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.messageTime = messageTime;
        this.messageDate = messageDate;
    }

    public MessageModel(String sender, String receiver, String message, String messageTime, String messageDate, String id) {
       this(sender, receiver, message, messageTime, messageDate);
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

}

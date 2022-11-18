package com.zerobudget.bookito.models.Chat;

import java.util.ArrayList;

public class MessageModel {
    private String sender;
    private String receiver;
    private String message;
    private String messageId;

    public MessageModel(String sender, String receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
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

    public static ArrayList<MessageModel> getMessages(String user1, String user2) {
        //TODO get un array of message between two users (maybe could be useful?)
        return null;
    }
}

package com.zerobudget.bookito.models.Chat;

public class MessageModel {
    private String sender;
    private String receiver;
    private String message;

    public MessageModel(String sender, String receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }
}

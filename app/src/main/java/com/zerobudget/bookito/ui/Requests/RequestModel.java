package com.zerobudget.bookito.ui.Requests;

import com.zerobudget.bookito.ui.users.UserModel;

import java.util.HashMap;
import java.util.Map;

public class RequestModel {
    private String requestedBook; //isbn libro richiesto
    private String sender; //id utente che fa la richiesta
    private String receiver; //id utente che RICEVE la richiesta (id utente attuale basically)
    private String status; //stato della richiesta, può assumere 3 valori: undefined, refused and accepted
    private String thumbnail;
    private String type; //Scambio, Prestito o Regalo
    private String title;
    private UserModel senderModel;
    private String requestId;

    public RequestModel() {}

    public RequestModel(String requestedBook, String requester, String recipient, String status, String thumbnail, String type, String title, String id) {
        this.requestedBook = requestedBook;
        this.sender = requester;
        this.receiver = recipient;
        this.status = status;
        this.thumbnail = thumbnail;
        this.type = type;
        this.title = title;
        this.requestId = id;
    }

    public Map<String, String> serialize() {

        Map<String, String> bookMap = new HashMap<>();
        bookMap.put("receiver", this.getReceiver());
        bookMap.put("requestedBook", this.getRequestedBook());
        bookMap.put("sender", this.getSender());
        bookMap.put("status", this.getStatus());
        bookMap.put("thumbnail", this.getThumbnail());
        bookMap.put("title", this.getTitle());
        bookMap.put("type", this.getType());


        return bookMap;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRequestedBook() {
        return requestedBook;
    }

    public String getSender() {
        return sender;
    }

    public String getStatus() {
        return status;
    }

    public UserModel getSenderModel() { return this.senderModel; }

    public void setRequestedBook(String requestedBook) {
        this.requestedBook = requestedBook;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setSenderModel(UserModel u) { this.senderModel = u; }

    public String getrequestId() {
        return this.requestId;
    }
}



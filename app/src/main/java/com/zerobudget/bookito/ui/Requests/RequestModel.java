package com.zerobudget.bookito.ui.Requests;

import java.util.HashMap;
import java.util.Map;

public class RequestModel {
    private String requestedBook; //isbn libro richiesto
    private String requester; //id utente che fa la richiesta
    private String recipient; //id utente che RICEVE la richiesta (id utente attuale basically)
    private String status; //stato della richiesta, può assumere 3 valori: undefined, refused and accepted
    private String thumbnail;
    private String type; //Scambio, Prestito o Regalo
    private String title;

    public RequestModel() {}

    public RequestModel(String requestedBook, String requester, String recipient, String status, String thumbnail, String type, String title) {
        this.requestedBook = requestedBook;
        this.requester = requester;
        this.recipient = recipient;
        this.status = status;
        this.thumbnail = thumbnail;
        this.type = type;
        this.title = title;
    }

    public Map<String, String> serialize() {

        Map<String, String> bookMap = new HashMap<>();
        bookMap.put("recipient", this.getRecipient());
        bookMap.put("requestedBook", this.getRequestedBook());
        bookMap.put("requester", this.getRequester());
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

    public String getRequester() {
        return requester;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getStatus() {
        return status;
    }

    public void setRequestedBook(String requestedBook) {
        this.requestedBook = requestedBook;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
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
}



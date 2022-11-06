package com.zerobudget.bookito.ui.Requests;

import java.util.Date;

public class RequestModel {
    private String requestedBook; //isbn libro richiesto
    private String requester; //id utente che fa la richiesta
    private String recipient; //id utente che RICEVE la richiesta (id utente attuale basically)
    private String status; //stato della richiesta, può assumere 3 valori: undefined, refused and accepted

    public RequestModel() {}

    public RequestModel(String requestedBook, String requester, String recipient, String status) {
        this.requestedBook = requestedBook;
        this.requester = requester;
        this.recipient = recipient;
        this.status = status;
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
}



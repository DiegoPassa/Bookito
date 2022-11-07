package com.zerobudget.bookito.ui.Requests;

import java.util.Date;

public class RequestTradeModel extends RequestModel{
    private String requestTradeBook; //libro che l'utente attuale richiede all'altro utente

    public RequestTradeModel() {}

    public RequestTradeModel(String requestedBook, String requester, String recipient, String status, String thumbnail, String title, String requestTradeBook) {
        super(requestedBook, requester, recipient, status, thumbnail, title);
        this.requestTradeBook = requestTradeBook;
    }
}

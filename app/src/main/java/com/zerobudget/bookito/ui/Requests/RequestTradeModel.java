package com.zerobudget.bookito.ui.Requests;

import java.util.Date;

public class RequestTradeModel extends RequestModel{
    private String requestTradeBook; //libro che l'utente attuale richiede all'altro utente

    public RequestTradeModel(String requestedBook, String requester, String recipient, String status, String requestTradeBook) {
        super(requestedBook, requester, recipient, status);
        this.requestTradeBook = requestTradeBook;
    }
}

package com.zerobudget.bookito.models.Requests;

public class RequestTradeModel extends RequestModel{
    private String requestTradeBook; //libro che l'utente attuale richiede all'altro utente

    public RequestTradeModel() {}

    public RequestTradeModel(String requestedBook, String requester, String recipient, String status, String thumbnail, String type, String title, String id, String requestTradeBook) {
        super(requestedBook, requester, recipient, status, thumbnail, type, title, id);
        this.requestTradeBook = requestTradeBook;
    }
}
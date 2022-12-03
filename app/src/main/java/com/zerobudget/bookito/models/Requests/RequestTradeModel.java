package com.zerobudget.bookito.models.Requests;

public class RequestTradeModel extends RequestModel {
    private String requestTradeBook; //libro che l'utente attuale richiede all'altro utente

    public RequestTradeModel() {
    }

    public RequestTradeModel(String requestedBook, String requester, String recipient, String status, String thumbnail, String type, String title, String id, String requestTradeBook, String note) {
        super(requestedBook, requester, recipient, status, thumbnail, type, title, id, note);
        this.requestTradeBook = requestTradeBook;
    }

    public String getRequestTradeBook() {
        return requestTradeBook;
    }

    public void setRequestTradeBook(String requestTradeBook) {
        this.requestTradeBook = requestTradeBook;
    }

    @Override
    public String toString() {
        return "RequestTradeModel{" +
                "requestedBook='" + requestedBook + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", status='" + status + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", otherUser=" + otherUser +
                ", requestId='" + requestId + '\'' +
                ", note='" + note + '\'' +
                ", requestTradeBook='" + requestTradeBook + '\'' +
                '}';
    }
}
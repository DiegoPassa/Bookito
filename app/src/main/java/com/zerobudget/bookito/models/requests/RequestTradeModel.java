package com.zerobudget.bookito.models.requests;

public class RequestTradeModel extends RequestModel {
    private String requestTradeBook; //libro che l'utente attuale richiede all'altro utente
    private String thumbnailBookTrade; //copertina del libro che il current usr richede all'altro utente
    private String titleBookTrade; //titolo del libro che il current usr richiede per lo scambio

    public RequestTradeModel() {
    }

    public RequestTradeModel(String requestedBook, String requester, String recipient, String status, String thumbnail, String type, String title, String id, String requestTradeBook, String note, String thumbnailBookTrade, String titleBookTrade) {
        super(requestedBook, requester, recipient, status, thumbnail, type, title, id, note);
        this.requestTradeBook = requestTradeBook;
        this.thumbnailBookTrade = thumbnailBookTrade;
        this.titleBookTrade = titleBookTrade;
    }

    public String getRequestTradeBook() {
        return requestTradeBook;
    }

    public void setRequestTradeBook(String requestTradeBook) {
        this.requestTradeBook = requestTradeBook;
    }

    public String getThumbnailBookTrade() {
        return thumbnailBookTrade;
    }

    public void setThumbnailBookTrade(String thumbnailBookTrade) {
        this.thumbnailBookTrade = thumbnailBookTrade;
    }

    public String getTitleBookTrade() {
        return titleBookTrade;
    }

    public void setTitleBookTrade(String titleBookTrade) {
        this.titleBookTrade = titleBookTrade;
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
                ", thumbnailBookTrade?'" + thumbnailBookTrade + '\'' +
                '}';
    }
}
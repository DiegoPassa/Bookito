package com.zerobudget.bookito.models.requests;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class RequestTradeModel extends RequestModel {
    private String requestTradeBook; //libro che l'utente attuale richiede all'altro utente
    private String thumbnailBookTrade; //copertina del libro che il current usr richede all'altro utente
    private String titleBookTrade; //titolo del libro che il current usr richiede per lo scambio
    private boolean senderConfirm = false;
    private boolean receiverConfirm = false;

    public RequestTradeModel() {
    }

    public RequestTradeModel(String requestedBook, String requester, String recipient, String status, String thumbnail, String type, String title, String id, String requestTradeBook, String note, String thumbnailBookTrade, String titleBookTrade, boolean senderConfirm, boolean receiverConfirm) {
        super(requestedBook, requester, recipient, status, thumbnail, type, title, id, note);
        this.requestTradeBook = requestTradeBook;
        this.thumbnailBookTrade = thumbnailBookTrade;
        this.titleBookTrade = titleBookTrade;
        this.senderConfirm = senderConfirm;
        this.receiverConfirm = receiverConfirm;
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = (HashMap<String, Object>) super.serialize();
        map.put("senderConfirm", senderConfirm);
        map.put("receiverConfirm", receiverConfirm);

        return map;
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

    public boolean isSenderConfirm() {
        return senderConfirm;
    }

    public void setSenderConfirm(boolean senderConfirm) {
        this.senderConfirm = senderConfirm;
    }

    public boolean isReceiverConfirm() {
        return receiverConfirm;
    }

    public void setReceiverConfirm(boolean receiverConfirm) {
        this.receiverConfirm = receiverConfirm;
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
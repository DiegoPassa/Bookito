package com.zerobudget.bookito.models.Requests;

import com.google.firebase.Timestamp;

import java.util.Date;

public class RequestShareModel extends RequestModel {
    private Timestamp date; //data di restituzione

    public RequestShareModel(){}

    public RequestShareModel(String requestedBook, String requester, String recipient, String status, String thumbnail, String type, String title, String id, Timestamp date) {
        super(requestedBook, requester, recipient, status, thumbnail, type, title, id);
        this.date = date;
    }
}

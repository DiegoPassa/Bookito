package com.zerobudget.bookito.ui.Requests;

import java.util.Date;

public class RequestShareModel extends RequestModel {
    private Date date; //data di restituzione

    public RequestShareModel(){}

    public RequestShareModel(String requestedBook, String requester, String recipient, String status, String thumbnail, String title, Date date) {
        super(requestedBook, requester, recipient, status, thumbnail, title);
        this.date = date;
    }
}

package com.zerobudget.bookito.ui.Requests;

import java.util.Date;

public class RequestShareModel extends RequestModel {
    private Date date; //data di restituzione

    public RequestShareModel(){}

    public RequestShareModel(String requestedBook, String requester, String recipient, String status, Date date) {
        super(requestedBook, requester, recipient, status);
        this.date = date;
    }
}

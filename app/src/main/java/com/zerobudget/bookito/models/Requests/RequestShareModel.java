package com.zerobudget.bookito.models.Requests;

import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RequestShareModel extends RequestModel {
    private Timestamp date; //data di restituzione

    public RequestShareModel(){}

    public RequestShareModel(String requestedBook, String requester, String recipient, String status, String thumbnail, String type, String title, String id, Timestamp date, String note) {
        super(requestedBook, requester, recipient, status, thumbnail, type, title, id, note);
        this.date = date;
    }

    public Date getDate() {
        return this.date.toDate();
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> ser = (HashMap<String, Object>) super.serialize();
        ser.put("date", this.date);
        return ser;
    }
}

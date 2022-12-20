package com.zerobudget.bookito.models.requests;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.zerobudget.bookito.models.users.UserModel;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class RequestModel {
    protected String requestedBook; //isbn libro richiesto
    protected String sender; //id utente che fa la richiesta
    protected String receiver; //id utente che RICEVE la richiesta (id utente attuale basically)
    protected String status; //valori: undefined, refused, concluded, closed, accepted
    protected String thumbnail;
    protected String type; //Scambio, Prestito o Regalo
    protected String title;
    protected UserModel otherUser;
    protected String requestId;
    protected String note;


    public RequestModel() {
    }

    public RequestModel(String requestedBook, String requester, String recipient, String status, String thumbnail, String type, String title, String id, String note) {
        this.requestedBook = requestedBook;
        this.sender = requester;
        this.receiver = recipient;
        this.status = status;
        this.thumbnail = thumbnail;
        this.type = type;
        this.title = title;
        this.requestId = id;
        this.note = note;
    }

    public Map<String, Object> serialize() {

        Map<String, Object> bookMap = new HashMap<>();

        bookMap.put("receiver", this.getReceiver());
        bookMap.put("requestedBook", this.getRequestedBook());
        bookMap.put("sender", this.getSender());
        bookMap.put("status", this.getStatus());
        bookMap.put("thumbnail", this.getThumbnail());
        bookMap.put("title", this.getTitle());
        bookMap.put("type", this.getType());
        bookMap.put("note", this.getNote());
        bookMap.put("timestamp", new Timestamp(System.currentTimeMillis()).getTime());

        return bookMap;
    }

    public Task<DocumentSnapshot> queryOtherUser(FirebaseFirestore db, String id) {
        return db.collection("users").document(id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserModel u = task.getResult().toObject(UserModel.class);
                        this.setOtherUser(u);
                    }
                });
    }

    public static RequestModel getRequestModel(String type, DocumentSnapshot o) {
        RequestModel r = new RequestModel();
        switch (type) {
            case ("Regalo"): {
                r = o.toObject(RequestModel.class);
                break;
            }
            case ("Prestito"): {
                r = o.toObject(RequestShareModel.class);
                break;
            }
            case ("Scambio"): {
                r = o.toObject(RequestTradeModel.class);
                break;
            }
        }
        r.setRequestId(o.getId());
        return r;
    }

    public String getRequestedBook() {
        return requestedBook;
    }

    public void setRequestedBook(String requestedBook) {
        this.requestedBook = requestedBook;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UserModel getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(UserModel otherUser) {
        this.otherUser = otherUser;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "RequestModel{" +
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
                '}';
    }
}



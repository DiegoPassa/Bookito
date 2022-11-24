package com.zerobudget.bookito.models.Requests;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zerobudget.bookito.models.users.UserModel;

import java.sql.Time;
import java.util.HashMap;
import java.util.Map;

public class RequestModel {
    private String requestedBook; //isbn libro richiesto
    private String sender; //id utente che fa la richiesta
    private String receiver; //id utente che RICEVE la richiesta (id utente attuale basically)
    private String status; //stato della richiesta, può assumere 3 valori: undefined, refused and accepted
    private String thumbnail;
    private String type; //Scambio, Prestito o Regalo
    private String title;
    private UserModel otherUser;
    private String requestId;


    public RequestModel() {}

    public RequestModel(String requestedBook, String requester, String recipient, String status, String thumbnail, String type, String title, String id) {
        this.requestedBook = requestedBook;
        this.sender = requester;
        this.receiver = recipient;
        this.status = status;
        this.thumbnail = thumbnail;
        this.type = type;
        this.title = title;
        this.requestId = id;
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


        return bookMap;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRequestedBook() {
        return requestedBook;
    }

    public String getSender() {
        return sender;
    }

    public String getStatus() {
        return status;
    }

    public UserModel getOtherUser() { return this.otherUser; }

    public void setRequestedBook(String requestedBook) {
        this.requestedBook = requestedBook;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setOtherUser(UserModel u) { this.otherUser = u; }

    public String getrequestId() {
        return this.requestId;
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

        switch (type) {
            case ("Regalo"): {
                return new RequestModel((String) o.get("requestedBook"), (String) o.get("sender"), (String) o.get("receiver"), (String) o.get("status"), (String)o.get("thumbnail"), type, (String)o.get("title"), o.getId());
            }
            case("Prestito"): {
                Log.d("COSTRUISCO", ""+o.get("date"));
                return new RequestShareModel((String) o.get("requestedBook"), (String) o.get("sender"), (String) o.get("receiver"), (String) o.get("status"), (String)o.get("thumbnail"),  type, (String) o.get("title"), o.getId(), (Timestamp) o.get("date"));
            }
            case("Scambio"): {
                return new RequestTradeModel((String) o.get("requestedBook"), (String) o.get("sender"), (String) o.get("receiver"), (String) o.get("status"), (String) o.get("thumbnail"), type, (String) o.get("title"), o.getId(), (String) o.get("requested_book"));
            }
        }
        return null;
    }
}



package com.zerobudget.bookito.models.Notification;

import com.zerobudget.bookito.models.users.UserModel;

import java.util.HashMap;

public class NotificationModel {
    private String actioner;
    private String type;
    private String notificationId;
    private String body;
    private String title;
    private String requestId;
    private String book_thumb;
    private UserModel userModel;
    //private RequestModel request;

    public NotificationModel(String actioner, String type, String notificationId, String body, String title, String requestID/*RequestModel request */, String book_thumb) {
        this.actioner = actioner;
        this.type = type;
        this.notificationId = notificationId;
        this.body = body;
        this.title = title;
        this.requestId = requestID;
        this.book_thumb = book_thumb;
        //this.request = request;
        //TODO invece di salvarci solo ID richiesta, ci possiamo salvare tutti i dati relativi a quella richiesta (così ci evitiamo di fare una query ogni volta per ottenere le info di quella richiesta)
    }

    public NotificationModel() {}

    public HashMap<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("actioner", this.actioner);
        map.put("type", this.type);
        map.put("notificationId", this.notificationId);
        map.put("body", this.body);
        map.put("title", this.title);

        return map;
    }

    public String toString() {
        return serialize().toString();
    }

    public String getActioner() {
        return actioner;
    }

    public void setActioner(String actioner) {
        this.actioner = actioner;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getBook_thumb() {
        return book_thumb;
    }

    public void setBook_thumb(String book_thumb) {
        this.book_thumb = book_thumb;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }
}

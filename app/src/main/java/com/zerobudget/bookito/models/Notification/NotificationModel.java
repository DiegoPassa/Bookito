package com.zerobudget.bookito.models.Notification;

import com.zerobudget.bookito.models.Requests.RequestModel;
import com.zerobudget.bookito.models.users.UserModel;

import java.util.HashMap;

public class NotificationModel {
    private String actionerId;
    private String type;
    private String notificationId;
    private String body;
    private String title;
    private String book_thumb;
    private UserModel actioner;
    private RequestModel request;

    public NotificationModel(String actionerId, String type, String body, String title, String book_thumb, RequestModel request, UserModel actioner) {
        this.type = type;
        this.body = body;
        this.title = title;
        this.book_thumb = book_thumb;
        this.actionerId = actionerId;
        this.request = request;
        this.actioner = actioner;
    }

    public NotificationModel() {}

    public HashMap<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("actionerId", this.actioner);
        map.put("type", this.type);
        map.put("notificationId", this.notificationId);
        map.put("body", this.body);
        map.put("title", this.title);
        map.put("book_thumb", this.book_thumb);
        map.put("request", request.serialize());
        map.put("actioner", actioner.serialize());

        return map;
    }

    public String toString() {
        return serialize().toString();
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


    public String getBook_thumb() {
        return book_thumb;
    }

    public void setBook_thumb(String book_thumb) {
        this.book_thumb = book_thumb;
    }

    public UserModel getUserModel() {
        return actioner;
    }

    public void setUserModel(UserModel userModel) {
        this.actioner = userModel;
    }

    public UserModel getActioner() {
        return actioner;
    }

    public void setActioner(UserModel actioner) {
        this.actioner = actioner;
    }

    public RequestModel getRequest() {
        return request;
    }

    public void setRequest(RequestModel request) {
        this.request = request;
    }

    public String getActionerId() {
        return actionerId;
    }

    public void setActionerId(String actionerId) {
        this.actionerId = actionerId;
    }
}

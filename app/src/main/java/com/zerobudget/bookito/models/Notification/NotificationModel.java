package com.zerobudget.bookito.models.Notification;

import java.util.HashMap;

public class NotificationModel {
    private String actioner;
    private String type;
    private String notificationId;
    private String body;
    private String title;

    public NotificationModel(String actioner, String type, String notificationId, String body, String title) {
        this.actioner = actioner;
        this.type = type;
        this.notificationId = notificationId;
        this.body = body;
        this.title = title;
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
}

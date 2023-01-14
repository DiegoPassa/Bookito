package com.zerobudget.bookito.models.notification;

import com.zerobudget.bookito.models.requests.RequestModel;
import com.zerobudget.bookito.models.users.UserModel;

import java.util.HashMap;

public class NotificationModel implements Comparable<NotificationModel>{
    private String actionerId;
    private String type;
    private String notificationId;
    private String body;
    private String title;
    private String book_thumb;
    private UserModel actioner;
    private RequestModel request;

    long timestamp;

    public NotificationModel(String actionerId, String type, String body, String title, String book_thumb, RequestModel request, UserModel actioner, long time) {
        this.type = type;
        this.body = body;
        this.title = title;
        this.book_thumb = book_thumb;
        this.actionerId = actionerId;
        this.request = request;
        this.actioner = actioner;
        this.timestamp = time;
    }

    public NotificationModel() {}

    public HashMap<String, Object> serialize() {

        HashMap<String,  Object> requestSerialized = (HashMap<String, Object>) request.serialize();
        requestSerialized.remove("date");
        requestSerialized.remove("requestTradeBook");

        HashMap<String, Object> map = new HashMap<>();
        HashMap<String, Object> data_notify = new HashMap<>();
        data_notify.put("actionerId", this.actionerId);
        data_notify.put("type", this.type);
        data_notify.put("notificationId", this.notificationId);
        data_notify.put("timestamp", this.timestamp);
        data_notify.put("body", this.body);
        data_notify.put("title", this.title);
        data_notify.put("book_thumb", this.book_thumb);
        map.put("request", requestSerialized);
        map.put("actioner", actioner.serialize());
        map.put("data_notify", data_notify);

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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(NotificationModel notificationModel) {
        return this.getTimestamp() <= notificationModel.getTimestamp() ? 1 : -1;
    }
}

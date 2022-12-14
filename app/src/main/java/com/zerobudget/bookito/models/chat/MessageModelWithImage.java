package com.zerobudget.bookito.models.chat;

import java.util.Map;

public class MessageModelWithImage extends MessageModel {
    private String thumbnailBookRequested;

    public MessageModelWithImage() {
    }

    public MessageModelWithImage(String thumbnailBookRequested, String sender, String receiver, String message, String status, long messageSentAt) {
        super(sender, receiver, message, status,messageSentAt);
        this.thumbnailBookRequested = thumbnailBookRequested;
    }

    public MessageModelWithImage(String thumbnailBookRequested, String sender, String receiver, String message, String status, long messageSentAt, String id) {
        super(sender, receiver, message, status, messageSentAt, id);
        this.thumbnailBookRequested = thumbnailBookRequested;
    }


    public String getThumbnailBookRequested() {
        return thumbnailBookRequested;
    }

    public void setThumbnailBookRequested(String thumbnailBookRequested) {
        this.thumbnailBookRequested = thumbnailBookRequested;
    }

    public Map<String, Object> serializeImage() {
        Map<String, Object> map = super.serialize();
        map.put("thumbnailBookRequested", this.thumbnailBookRequested);
        return map;
    }
}

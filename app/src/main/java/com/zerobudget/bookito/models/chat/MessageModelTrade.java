package com.zerobudget.bookito.models.chat;

import java.util.Map;

public class MessageModelTrade extends MessageModel {
    private String isbnBookTrade;
    private String thumbnailBookTrade;

    public MessageModelTrade() {
    }

    public MessageModelTrade(String isbnBookTrade, String thumbnailBookTrade, String sender, String receiver, String message, String status, long messageSentAt/*, String messageTime, String messageDate*/) {
        super(sender, receiver, message, status, messageSentAt/*, messageTime, messageDate*/);
        this.isbnBookTrade = isbnBookTrade;
        this.thumbnailBookTrade = thumbnailBookTrade;
    }

    public MessageModelTrade(String isbnBookTrade, String thumbnailBookTrade, String sender, String receiver, String message, String status, long messageSentAt, String id) {
        super(sender, receiver, message, status, messageSentAt, id);
        this.isbnBookTrade = isbnBookTrade;
        this.thumbnailBookTrade = thumbnailBookTrade;
    }

    public String getIsbnBookTrade() {
        return isbnBookTrade;
    }

    public void setIsbnBookTrade(String isbnBookTrade) {
        this.isbnBookTrade = isbnBookTrade;
    }

    public String getThumbnailBookTrade() {
        return thumbnailBookTrade;
    }

    public void setThumbnailBookTrade(String thumbnailBookTrade) {
        this.thumbnailBookTrade = thumbnailBookTrade;
    }


    public Map<String, Object> serializeTrade() {
        Map<String, Object> map = super.serialize();
        map.put("isbnBookTrade", this.isbnBookTrade);
        map.put("thumbnailBookTrade", this.thumbnailBookTrade);

        return map;
    }
}

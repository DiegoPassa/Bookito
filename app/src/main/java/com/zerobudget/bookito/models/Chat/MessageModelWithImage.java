package com.zerobudget.bookito.models.Chat;

public class MessageModelWithImage extends MessageModel{
    private String thumbnailBookRequested;

    public MessageModelWithImage(){}

    public MessageModelWithImage(String thumbnailBookRequested, String sender, String receiver, String message, String messageTime, String messageDate){
        super(sender, receiver, message, messageTime, messageDate);
        this.thumbnailBookRequested = thumbnailBookRequested;
    }

    public MessageModelWithImage(String thumbnailBookRequested, String sender, String receiver, String message, String messageTime, String messageDate, String id) {
        super(sender, receiver, message, messageTime, messageDate, id);
        this.thumbnailBookRequested = thumbnailBookRequested;
    }


    public String getThumbnailBookRequested() {
        return thumbnailBookRequested;
    }

    public void setThumbnailBookRequested(String thumbnailBookRequested) {
        this.thumbnailBookRequested = thumbnailBookRequested;
    }
}

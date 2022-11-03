package com.zerobudget.bookito.ui.library;

public class BookModel{
    private final int thumbnail;
    private final String title;

    public BookModel(int thumbnail, String title) {
        this.thumbnail = thumbnail;
        this.title = title;
    }

    public int getThumbnail() {
        return thumbnail;
    }

    public String getTitle() {
        return title;
    }
}

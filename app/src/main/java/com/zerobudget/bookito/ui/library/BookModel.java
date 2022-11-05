package com.zerobudget.bookito.ui.library;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class BookModel{
    // TODO: completare con i dati relativi al libro mancanti
    private String thumbnail = "";
    private String isbn = "";
    private String title = "";
    private String author = "";
    private String description = "";


    public BookModel(@NonNull String thumbnail, @NonNull String isbn, @NonNull String title, @NonNull String author, @NonNull String description) {
        this.thumbnail = thumbnail;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.description = description;
    }

    public BookModel() {}

    public Map<String, String> serialize() {

        Map<String, String> bookMap = new HashMap<>();
        bookMap.put("thumbnail", this.getThumbnail());
        bookMap.put("title", this.getTitle());
        bookMap.put("author", this.getAuthor());
        bookMap.put("isbn", this.getIsbn());
        bookMap.put("description", this.getDescription());

        return bookMap;
    }

    public String getIsbn() { return this.isbn; }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

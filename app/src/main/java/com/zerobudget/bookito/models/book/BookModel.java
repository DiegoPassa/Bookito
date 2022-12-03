package com.zerobudget.bookito.models.book;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class BookModel {

    private String author;
    private String description;
    private String isbn;
    private String thumbnail;
    private String title;
    private String type; // Scambio, Prestito, Regalo
    private boolean status; //true se enabled, false se disabled

    public static final boolean ENABLE = true;
    public static final boolean DISABLE = false;


    public BookModel(@NonNull String thumbnail, @NonNull String isbn, @NonNull String title, @NonNull String author, @NonNull String description, @NonNull String type, boolean status) {
        this.thumbnail = thumbnail;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.description = description;
        this.type = type;
        this.status = status;
    }

    public BookModel() {
    }

    public Map<String, Object> serialize() {

        Map<String, Object> bookMap = new HashMap<>();
        bookMap.put("thumbnail", this.getThumbnail());
        bookMap.put("title", this.getTitle());
        bookMap.put("author", this.getAuthor());
        bookMap.put("isbn", this.getIsbn());
        bookMap.put("description", this.getDescription());
        bookMap.put("type", this.getType());
        bookMap.put("status", this.status);

        return bookMap;
    }

    public String getIsbn() {
        return this.isbn;
    }

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

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }


    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "BookModel{" +
                "author='" + author + '\'' +
                ", description='" + description + '\'' +
                ", isbn='" + isbn + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", status ='" + status + '\'' +
                '}';
    }
}

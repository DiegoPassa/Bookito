package com.zerobudget.bookito.ui.library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BookModel{
    // TODO: completare con i dati relativi al libro mancanti
    private String thumbnail;
    private String isbn;
    private String title;
    private String author;


    public BookModel(String thumbnail, String isbn, String title, String author) {
        this.thumbnail = thumbnail;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
    }

    public BookModel() {
        thumbnail = "";
        isbn = "";
        title = "";
        author = "";
    }

    public Map<String, String> serialize() {

        Map<String, String> bookMap = new HashMap<>();
        bookMap.put("thumbnail", this.getThumbnail());
        bookMap.put("title", this.getTitle());
        bookMap.put("authors", this.getAuthor());
        bookMap.put("isbn", this.getIsbn());
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
}

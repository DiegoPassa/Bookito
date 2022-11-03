package com.zerobudget.bookito.ui.library;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BookModel{
    // TODO: completare con i dati relativi al libro mancanti
    private String thumbnail;
    private String isbn;
    private String title;
    private ArrayList<String> authors;


    public BookModel(String thumbnail, String title, ArrayList<String> authors) {
        this.thumbnail = thumbnail;
        this.title = title;
        this.authors = authors;
    }

    public BookModel() {
        thumbnail = "";
        title = "";
        authors = null;
    }

    public Map<String, String> serialize() {

        Map<String, String> bookMap = new HashMap<>();
        bookMap.put("thumbnail", this.getThumbnail());
        bookMap.put("title", this.getTitle());
        bookMap.put("authors", this.getAuthors().get(0));
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

    public ArrayList<String> getAuthors() {
        return authors;
    }

    public void setAuthors(ArrayList<String> authors) {
        this.authors = authors;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
}

package com.zerobudget.bookito.ui.library;

import java.util.ArrayList;

public class BookModel{
    private String thumbnail;
    //private String isbn;
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
}

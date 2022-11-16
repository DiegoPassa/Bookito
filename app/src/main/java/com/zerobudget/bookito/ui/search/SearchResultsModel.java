package com.zerobudget.bookito.ui.search;

import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.models.users.UserModel;

class SearchResultsModel {
    private BookModel book;
    private UserModel user;

    public SearchResultsModel(BookModel book, UserModel user) {
        this.book = book;
        this.user = user;
    }

    public BookModel getBook() {
        return book;
    }

    public void setBook(BookModel book) {
        this.book = book;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "SearchResultsModel{" +
                "book=" + book +
                ", user=" + user +
                '}';
    }
}

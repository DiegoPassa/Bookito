package com.zerobudget.bookito.models.search;

import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.models.users.UserModel;

public class SearchResultsModel implements Comparable<SearchResultsModel> {
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

    @Override
    public int compareTo(SearchResultsModel searchResultsModel) {
        return this.getBook().getTitle().compareTo(searchResultsModel.getBook().getTitle());
    }
}

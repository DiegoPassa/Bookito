package com.zerobudget.bookito.models.users;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.zerobudget.bookito.models.book.BookModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public class UserLibrary extends UserModel {
    protected List<BookModel> books;

    public UserLibrary(String first_name, String last_name, String telephone, String township, String city, HashMap<String, Object> karma, Boolean hasPicture, String notificationToken) {
        super(first_name, last_name, telephone, township, city, karma, hasPicture, notificationToken);
    }

    public UserLibrary(UserModel u, List<BookModel> bookModels) {
        super(u.getFirstName(), u.getLastName(), u.getTelephone(), u.getTownship(), u.getCity(), u.getKarma(), u.isHasPicture(), u.getNotificationToken());
        books = bookModels;
    }

    public UserLibrary(UserModel u) {
        super(u.getFirstName(), u.getLastName(), u.getTelephone(), u.getTownship(), u.getCity(), u.getKarma(), u.isHasPicture(), u.getNotificationToken());
        books = new ArrayList<>();
    }

    public UserLibrary() {
    }

    public static ArrayList<BookModel> loadLibrary(DocumentSnapshot doc) {
        ArrayList<BookModel> library = new ArrayList<>();
        ArrayList<Object> results = (ArrayList<Object>) doc.get("books");

        for (Object book : results) {
            HashMap<Object, Object> map = (HashMap<Object, Object>) book;
            BookModel newBook = new BookModel();
            newBook.setIsbn((String) map.get("isbn"));
            newBook.setThumbnail((String) map.get("thumbnail"));
            newBook.setTitle((String) map.get("title"));
            newBook.setAuthor((String) map.get("author"));
            library.add(newBook);
        }
        return library;
    }

    public List<BookModel> getBooks() {
        return books;
    }

    public void setBooks(List<BookModel> books) {
        this.books = books;
    }

    public void appendBook(BookModel book) {
        this.books.add(book);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> user_info = super.serialize();

        ArrayList<Map<String, Object>> libraryBooks = new ArrayList<>();

        for (BookModel book : books) {
            libraryBooks.add(book.serialize());
        }

        user_info.put("books", libraryBooks);

        return user_info;
    }

    @Override
    public String toString() {
        return "UserLibrary{" +
                "library=" + books +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", telephone='" + telephone + '\'' +
                ", township='" + township + '\'' +
                ", city='" + city + '\'' +
                ", notificationToken='" + notificationToken + '\'' +
                ", hasPicture=" + hasPicture +
                ", karma=" + karma +
                '}';
    }
}

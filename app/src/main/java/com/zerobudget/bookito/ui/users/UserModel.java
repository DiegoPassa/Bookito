package com.zerobudget.bookito.ui.users;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.zerobudget.bookito.ui.library.BookModel;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserModel {
    private String first_name;
    private String last_name;
    private String telephone;
    private String neighborhood;
    //private String id; id utente facilmente ottenibile con FirebaseAuth.getInstance().getCurrentUser().getId()
    private static UserModel currentUser; //modello dell'utente attuale

    private ArrayList<BookModel> library;
    private HashMap<String, Object> karma;

    public UserModel(){}

    public UserModel(String first_name, String last_name, String telephone, String neighborhood, ArrayList<BookModel> library) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.telephone = telephone;
        this.neighborhood = neighborhood;
        this.library = library;
    }

    public static void loadUser(UserModel user) {
        UserModel.currentUser = user;
    }



    public Map<String, Object> serialize() {
        HashMap<String, Object> user = new HashMap<>();
        ArrayList<Map<String, String>> userLibrary = new ArrayList<>();

        user.put("first_name", this.first_name);
        user.put("last_name", this.last_name);
        user.put("telephone", this.telephone);
        user.put("neighborhood", this.neighborhood);

        for (BookModel book : library) {
            userLibrary.add(book.serialize());
        }

        user.put("books", userLibrary);

        return user;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String name) {
        this.first_name = name;
    }

    public String getLast_name() { return last_name; }

    public String getTelephone() { return telephone; }

    public String getNeighborhood() { return neighborhood; }

    public HashMap<String, Object> getKarma() { return karma; }

    public static UserModel getCurrentUser() { return UserModel.currentUser; }

    public void setFirst_Name(String name) { this.first_name = name; }

    public void setLast_name(String name) { this.last_name = name; }

    public void setTelephone(String telephone) { this.telephone = telephone; }

    public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }

    public void setKarma(HashMap<String, Object> k) { this.karma = k; }

    public void setLibrary(ArrayList<BookModel> library) { this.library = library; }

    public void appendBook(BookModel book) { this.library.add(book); }


}

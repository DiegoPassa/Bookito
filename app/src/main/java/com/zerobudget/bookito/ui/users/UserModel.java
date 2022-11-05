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

    private ArrayList<BookModel> library;


    public UserModel(){}

    public UserModel(String first_name, String last_name, String telephone, String neighborhood, ArrayList<BookModel> library) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.telephone = telephone;
        this.neighborhood = neighborhood;
        this.library = library;
    }

    public static void loadUser(UserModel user) {

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



}

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
    private static UserLibrary currentUser; //modello dell'utente attuale

    private HashMap<String, Object> karma;

    public UserModel(){}

    public UserModel(String first_name, String last_name, String telephone, String neighborhood, HashMap<String, Object> karma) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.telephone = telephone;
        this.neighborhood = neighborhood;
        this.karma = karma;
    }

    public static void loadUser(UserModel user) {
        if (currentUser == null) {
            UserModel.currentUser = (UserLibrary) user;
        }
    }



    public Map<String, Object> serialize() {
        HashMap<String, Object> user = new HashMap<>();

        user.put("first_name", this.first_name);
        user.put("last_name", this.last_name);
        user.put("telephone", this.telephone);
        user.put("neighborhood", this.neighborhood);
        user.put("karma", this.karma);


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

    public static UserLibrary getCurrentUser() { return UserModel.currentUser; }

    public void setFirst_Name(String name) { this.first_name = name; }

    public void setLast_name(String name) { this.last_name = name; }

    public void setTelephone(String telephone) { this.telephone = telephone; }

    public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }

    public void setKarma(HashMap<String, Object> k) { this.karma = k; }

    public static UserModel getUserFromDocument(DocumentSnapshot result) {
        UserModel u = new UserModel();

        u.setFirst_name((String) result.get("first_name"));
        u.setLast_name((String) result.get("last_name"));
        u.setTelephone((String) result.get("telephone"));
        u.setNeighborhood((String) result.get("neighborhood"));
        u.setKarma(loadKarma(result));

        return u;
    }

    private static HashMap<String, Object> loadKarma(DocumentSnapshot doc) {

        return (HashMap<String, Object>) doc.get("karma");
    }


}

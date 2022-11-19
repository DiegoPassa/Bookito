package com.zerobudget.bookito.models.users;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserModel {
    private String first_name;
    private String last_name;
    private String telephone;
    private String neighborhood;
    //private String id; id utente facilmente ottenibile con FirebaseAuth.getInstance().getCurrentUser().getId()
    private static UserLibrary currentUser; //modello dell'utente attuale
    private String notificationToken;

    private boolean hasPicture = false;

    private HashMap<String, Object> karma;

    public UserModel() {
    }

    public UserModel(String first_name, String last_name, String telephone, String neighborhood, HashMap<String, Object> karma, Boolean hasPicture, String notificationToken) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.telephone = telephone;
        this.neighborhood = neighborhood;
        this.karma = karma;
        this.hasPicture = hasPicture;
        this.notificationToken = notificationToken;
    }

    public static void loadUser(UserModel user) {

        UserModel.currentUser = (UserLibrary) user;

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
        u.setNotificationToken((String) result.get("notificationToken"));
        u.setKarma(loadKarma(result));

        Boolean hasPicture = (Boolean) result.get("hasPicture");
        if (hasPicture != null) {
            u.setHasPicture(hasPicture);
        } else {
            u.setHasPicture(false);
        }

        return u;
    }

    private static HashMap<String, Object> loadKarma(DocumentSnapshot doc) {

        return (HashMap<String, Object>) doc.get("karma");
    }

    public boolean isHasPicture() {
        return hasPicture;
    }

    public void setHasPicture(boolean hasPicture) {
        this.hasPicture = hasPicture;
    }


    public String getNotificationToken() {
        return notificationToken;
    }

    public void setNotificationToken(String notificationToken) {
        this.notificationToken = notificationToken;
    }
}

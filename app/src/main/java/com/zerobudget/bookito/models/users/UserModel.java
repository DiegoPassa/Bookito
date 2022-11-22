package com.zerobudget.bookito.models.users;

import java.util.HashMap;
import java.util.Map;

public class UserModel {
    private String firstName;
    private String lastName;
    private String telephone;
    private String neighborhood;
    //private String id; id utente facilmente ottenibile con FirebaseAuth.getInstance().getCurrentUser().getId()
    private static UserLibrary currentUser; //modello dell'utente attuale
    private String notificationToken;

    private boolean hasPicture = false;

    private HashMap<String, Object> karma;

    public UserModel() {
    }

    public UserModel(String firstName, String lastName, String telephone, String neighborhood, HashMap<String, Object> karma, Boolean hasPicture, String notificationToken) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.telephone = telephone;
        this.neighborhood = neighborhood;
        this.karma = karma;
        this.hasPicture = hasPicture;
        this.notificationToken = notificationToken;
    }

    public static void loadUser(UserModel user) {

        UserModel.currentUser = (UserLibrary) user;

    }

    public static UserLibrary getCurrentUser() {
        return currentUser;
    }

/*    public static UserModel getUserFromDocument(DocumentSnapshot result) {
        UserModel u = new UserModel();

        u.setFirstName((String) result.get("first_name"));
        u.setLastName((String) result.get("last_name"));
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
    }*/

/*    private static HashMap<String, Object> loadKarma(DocumentSnapshot doc) {

        return (HashMap<String, Object>) doc.get("karma");
    }*/

    public static void setCurrentUser(UserLibrary currentUser) {
        UserModel.currentUser = currentUser;
    }

    public Map<String, Object> serialize() {
        HashMap<String, Object> user = new HashMap<>();

        user.put("first_name", this.firstName);
        user.put("last_name", this.lastName);
        user.put("telephone", this.telephone);
        user.put("neighborhood", this.neighborhood);
        user.put("karma", this.karma);


        return user;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getNotificationToken() {
        return notificationToken;
    }

    public void setNotificationToken(String notificationToken) {
        this.notificationToken = notificationToken;
    }

    public boolean isHasPicture() {
        return hasPicture;
    }

    public void setHasPicture(boolean hasPicture) {
        this.hasPicture = hasPicture;
    }

    public HashMap<String, Object> getKarma() {
        return karma;
    }

    public void setKarma(HashMap<String, Object> karma) {
        this.karma = karma;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", telephone='" + telephone + '\'' +
                ", neighborhood='" + neighborhood + '\'' +
                ", notificationToken='" + notificationToken + '\'' +
                ", hasPicture=" + hasPicture +
                ", karma=" + karma +
                '}';
    }
}

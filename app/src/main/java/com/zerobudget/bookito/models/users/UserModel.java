package com.zerobudget.bookito.models.users;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class UserModel {
    protected String firstName;
    protected String lastName;
    protected String telephone;
    protected String township = "";
    protected String city = "";
    //private String id; id utente facilmente ottenibile con FirebaseAuth.getInstance().getCurrentUser().getId()
    protected String notificationToken;
    protected boolean hasPicture = false;
    protected HashMap<String, Object> karma;

    public UserModel() {
    }

    public UserModel(String firstName, String lastName, String telephone, String township, String city, HashMap<String, Object> karma, Boolean hasPicture, String notificationToken) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.telephone = telephone;
        this.township = township;
        this.city = city;
        this.karma = karma;
        this.hasPicture = hasPicture;
        this.notificationToken = notificationToken;
    }

    public Map<String, Object> serialize() {
        HashMap<String, Object> user = new HashMap<>();

        user.put("first_name", this.firstName);
        user.put("last_name", this.lastName);
        user.put("telephone", this.telephone);
        user.put("township", this.township);
        user.put("city", this.city);
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

    public String getTownship() {
        return township;
    }

    public void setTownship(String township) {
        this.township = township;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "firstName='" + firstName + '\'' +
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

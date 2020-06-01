package com.marco_cavalli.lost_and_found.objects;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    public String userID;
    public String displayName;
    public String email;
    public String gender;
    public String city;
    public String birthday;

    public User(String userID, String displayName, String email, String gender, String city, String birthday) {
        this.userID = userID;
        this.displayName = displayName;
        this.email = email;
        this.gender = gender;
        this.city = city;
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public User(String userID, String displayName, String email) {
        this.userID = userID;
        this.displayName = displayName;
        this.email = email;
        this.gender = "";
        this.city = "";
        this.birthday ="";
    }
}
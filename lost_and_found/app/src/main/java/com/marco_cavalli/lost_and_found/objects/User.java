package com.marco_cavalli.lost_and_found.objects;
import com.google.firebase.database.IgnoreExtraProperties;
import com.marco_cavalli.lost_and_found.R;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User {

    private String icon;
    private String userID;
    private String displayName;
    private String email;
    private String phone;
    private int gender;
    private String city;
    private String birthday;
    private Map<String,PersonalObject> objs;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String userID, String displayName, String email) {
        this.icon = "";
        this.userID = userID;
        this.displayName = displayName;
        this.email = email;
        this.phone = "";
        this.gender = 0; //not_specified
        this.city = "";
        this.birthday ="";
        this.objs = new HashMap<>();
    }

    public User(String userID, String displayName, String email, int gender, String city, String birthday, Map<String,PersonalObject> objs) {
        this.userID = userID;
        this.displayName = displayName;
        this.email = email;
        this.phone = "";
        this.gender = gender;
        this.city = city;
        this.birthday = birthday;
        this.objs = objs;
        this.icon = "";
    }

    public User(String userID, String displayName, String email, String phone, int gender, String city, String birthday, Map<String,PersonalObject> objs) {
        this.userID = userID;
        this.displayName = displayName;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.city = city;
        this.birthday = birthday;
        this.objs = objs;
        this.icon = "";
    }


    public User(String userID, String displayName, String email, String phone, int gender, String city, String birthday, Map<String,PersonalObject> objs, String icon) {
        this.userID = userID;
        this.displayName = displayName;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.city = city;
        this.birthday = birthday;
        this.objs = objs;
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Map<String, PersonalObject> getObjs() {
        return objs;
    }

    public void setObjs(Map<String, PersonalObject> objs) {
        this.objs = objs;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "User{" +
                "icon='" + icon + '\'' +
                ", userID='" + userID + '\'' +
                ", displayName='" + displayName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", gender=" + gender +
                ", city='" + city + '\'' +
                ", birthday='" + birthday + '\'' +
                //", objs=" + objs +
                '}';
    }
}
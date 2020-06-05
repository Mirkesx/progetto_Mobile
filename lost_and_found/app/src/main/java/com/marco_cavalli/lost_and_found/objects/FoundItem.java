package com.marco_cavalli.lost_and_found.objects;

public class FoundItem {

    String id, user_name, date, icon, object_name, description;
    Double latitude, longitude;
    Boolean setFound;

    public FoundItem(String id, String user_name, String date, String icon, String object_name, String description, Double latitude, Double longitude) {
        this.id = id;
        this.user_name = user_name;
        this.date = date;
        this.icon = icon;
        this.object_name = object_name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        setFound = false;
    }

    public FoundItem() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getObject_name() {
        return object_name;
    }

    public void setObject_name(String object_name) {
        this.object_name = object_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Boolean getSetFound() {
        return setFound;
    }

    public void setSetFound(Boolean setFound) {
        this.setFound = setFound;
    }
}

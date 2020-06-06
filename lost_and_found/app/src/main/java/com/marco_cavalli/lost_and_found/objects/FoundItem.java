package com.marco_cavalli.lost_and_found.objects;

public class FoundItem {

    String id, user_id, user_name, date, icon, object_name, description, address, timestamp;
    Double latitude, longitude;
    Boolean setFound;

    public FoundItem(String id, String user_id, String user_name, String date, String icon, String object_name, String description, String address, Double latitude, Double longitude, String timestamp) {
        this.id = id;
        this.user_id = user_id;
        this.user_name = user_name;
        this.date = date;
        this.icon = icon;
        this.object_name = object_name;
        this.description = description;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        setFound = false;
    }

    public FoundItem(String id, String user_id, String user_name, String date, String icon, String object_name, String description, String address, Double latitude, Double longitude, String timestamp, Boolean setFound) {
        this.id = id;
        this.user_id = user_id;
        this.user_name = user_name;
        this.date = date;
        this.icon = icon;
        this.object_name = object_name;
        this.description = description;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.setFound = setFound;
    }

    public FoundItem() {
    }


    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }


    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

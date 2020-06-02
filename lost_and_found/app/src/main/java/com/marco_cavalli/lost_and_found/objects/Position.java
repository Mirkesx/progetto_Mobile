package com.marco_cavalli.lost_and_found.objects;

public class Position {

    Double latitude, longitude;
    String date, description;

    public Position(String date, String description) {
        this.date = date;
        this.description = description;
    }

    public Position(Double latitude, Double longitude, String date, String description) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Position(Double latitude, Double longitude, String date) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

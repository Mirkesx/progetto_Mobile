package com.marco_cavalli.lost_and_found.objects;

import java.util.Calendar;

public class Position {

    Double latitude, longitude;
    String date, description, pos_id, icon, address;

    public Position() {
    }

    public Position(String pos_id, String date, String description, Double latitude, Double longitude) {
        this.pos_id = pos_id;
        this.latitude = latitude;
        this.longitude = longitude;
        if(date != null)
            this.date = date;
        else
            this.date = today();
        this.description = description;
        this.address = "";
        this.icon = "";
    }

    public Position(String pos_id, String date, String description, Double latitude, Double longitude, String icon) {
        this.pos_id = pos_id;
        this.latitude = latitude;
        this.longitude = longitude;
        if(date != null)
            this.date = date;
        else
            this.date = today();
        this.description = description;
        this.address = "";
        this.icon = icon;
    }

    public Position(String pos_id, String date, String description, String address, Double latitude, Double longitude, String icon) {
        this.pos_id = pos_id;
        this.latitude = latitude;
        this.longitude = longitude;
        if(date != null)
            this.date = date;
        else
            this.date = today();
        this.description = description;
        this.address = address;
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getPos_id() {
        return pos_id;
    }

    public void setPos_id(String pos_id) {
        this.pos_id = pos_id;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    private String today() {
        String d, m, y;
        d = ""+Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        m = ""+Calendar.getInstance().get(Calendar.MONTH);
        y = ""+Calendar.getInstance().get(Calendar.YEAR);
        if(d.length() == 1) {
            d = "0"+d;
        }
        if(m.length() == 1) {
            m = "0"+d;
        }
        return d+"/"+m+"/"+y;
    }
}

package com.marco_cavalli.lost_and_found.objects;

import android.media.Image;

public class PersonalObject {
    Image icon;
    String name;
    Double latitude, longitude;
    String object_id;

    public PersonalObject(Image icon, String name, Double latitude, Double longitude, String object_id) {
        this.icon = icon;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.object_id = object_id;
    }

    public Image getIcon() {
        return icon;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getObject_id() {
        return object_id;
    }

    public void setObject_id(String object_id) {
        this.object_id = object_id;
    }
}

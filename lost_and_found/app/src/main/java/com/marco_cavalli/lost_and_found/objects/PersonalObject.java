package com.marco_cavalli.lost_and_found.objects;

import android.media.Image;

import java.util.ArrayList;

public class PersonalObject {
    Image icon;
    ArrayList<Position> positions;
    String object_id, name, description;

    public PersonalObject(Image icon, String name, String object_id) {
        this.icon = icon;
        this.name = name;
        this.description = "";
        this.positions = new ArrayList<>();
        this.object_id = object_id;
    }

    public PersonalObject(Image icon, String name, String description, String object_id) {
        this.icon = icon;
        this.name = name;
        this.description = description;
        this.positions = new ArrayList<>();
        this.object_id = object_id;
    }

    public PersonalObject(Image icon, String name, ArrayList<Position> positions, String object_id) {
        this.icon = icon;
        this.name = name;
        this.description = "";
        this.positions = positions;
        this.object_id = object_id;
    }

    public PersonalObject(Image icon, String name, String description, ArrayList<Position> positions, String object_id) {
        this.icon = icon;
        this.name = name;
        this.description = description;
        this.positions = positions;
        this.object_id = object_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Position> getPositions() {
        return positions;
    }

    public void setPositions(ArrayList<Position> positions) {
        this.positions = positions;
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

    public String getObject_id() {
        return object_id;
    }

    public void setObject_id(String object_id) {
        this.object_id = object_id;
    }
}

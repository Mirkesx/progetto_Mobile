package com.marco_cavalli.lost_and_found.objects;

import android.media.Image;

import java.util.HashMap;
import java.util.Map;

public class PersonalObject {
    Image icon;
    Map<String,Position> positions;
    String object_id, name, description;

    public PersonalObject(Image icon, String name, String object_id) {
        this.icon = icon;
        this.name = name;
        this.description = "";
        this.positions = new HashMap<>();
        this.object_id = object_id;
    }

    public PersonalObject(Image icon, String name, String description, String object_id) {
        this.icon = icon;
        this.name = name;
        this.description = description;
        this.positions = new HashMap<>();
        this.object_id = object_id;
    }

    public PersonalObject(Image icon, String name, Map<String,Position> positions, String object_id) {
        this.icon = icon;
        this.name = name;
        this.description = "";
        this.positions = positions;
        this.object_id = object_id;
    }

    public PersonalObject(Image icon, String name, String description, Map<String,Position> positions, String object_id) {
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

    public Map<String,Position> getPositions() {
        return positions;
    }

    @Override
    public String toString() {
        return "PersonalObject{" +
                "icon=" + icon +
                ", positions=" + positions +
                ", object_id='" + object_id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public void setPositions(Map<String,Position> positions) {
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

    public PersonalObject() {
    }

    public Position getLastPosition() {
        int last_position = positions.keySet().size()-1;
        if(last_position >= 0) {
            String lastKey = positions.keySet().toArray()[last_position].toString();
            return positions.get(lastKey);
        }
        return null;
    }
}

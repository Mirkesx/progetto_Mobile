package com.marco_cavalli.lost_and_found.objects;

import java.util.HashMap;
import java.util.Map;

public class PersonalObject {
    String icon;
    Map<String,Position> positions;
    String object_id, name, description;

    public PersonalObject() {
    }

    public PersonalObject(String icon, String name, String description, String object_id) {
        this.icon = icon;
        this.name = name;
        this.description = description;
        this.positions = new HashMap<>();
        this.object_id = object_id;
    }

    public PersonalObject(String icon, String name, String description, Map<String,Position> positions, String object_id) {
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
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

package com.marco_cavalli.lost_and_found.objects;

public class Temp {
    String text;
    String image;

    @Override
    public String toString() {
        return "Temp{" +
                "text='" + text + '\'' +
                ", image='" + image + '\'' +
                '}';
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Temp(String text, String image) {
        this.text = text;
        this.image = image;
    }
}

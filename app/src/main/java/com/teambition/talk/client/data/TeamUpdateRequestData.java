package com.teambition.talk.client.data;

import com.teambition.talk.entity.Prefs;

/**
 * Created by zeatual on 15/4/1.
 */
public class TeamUpdateRequestData {

    String name;
    String color;
    Prefs prefs;

    public TeamUpdateRequestData() {
    }

    public TeamUpdateRequestData(String name, String color, Prefs prefs) {
        this.name = name;
        this.color = color;
        this.prefs = prefs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Prefs getPrefs() {
        return prefs;
    }

    public void setPrefs(Prefs prefs) {
        this.prefs = prefs;
    }
}

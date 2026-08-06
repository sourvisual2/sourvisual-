package com.sourvisual.client.gui;

public enum MenuCategory {

    EFFECTS("effects"),
    VISUAL("visual"),
    SETTINGS("settings");

    public final String label;

    MenuCategory(String label) {
        this.label = label;
    }
}

package com.sourvisual.gui;

public enum Tab {
    VISUAL("Visual"),
    UTILITIES("Utilities"),
    KEYBINDS("Keybinds"),
    SETTINGS("Settings");

    public final String label;

    Tab(String label) {
        this.label = label;
    }
}

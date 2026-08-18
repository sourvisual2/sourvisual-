package com.sourvisual.config;

public class SourVisualConfig {

    public static class ThemePreset {
        public final String name;
        public final int color;

        public ThemePreset(String name, int color) {
            this.name = name;
            this.color = color;
        }
    }

    public enum HitboxColorMode {
        THEME, CUSTOM
    }

    public static final ThemePreset[] THEMES = {
            new ThemePreset("Galaxy",   0xFF8A5CFF),
            new ThemePreset("Crystal",  0xFF5CC8FF),
            new ThemePreset("Emerald",  0xFF3ECF8E),
            new ThemePreset("Amethyst", 0xFFB05CFF),
            new ThemePreset("Ruby",     0xFFFF4D6D),
            new ThemePreset("Sunset",   0xFFFF9F45),
            new ThemePreset("Ocean",    0xFF1E90A8),
            new ThemePreset("Gold",     0xFFE0B94D),
            new ThemePreset("Midnight", 0xFF2A3F8F),
            new ThemePreset("Coral",    0xFFFF7F6B),
    };

    public static int selectedThemeIndex = 0;

    public static boolean wmEnabled = true;
    public static int wmX = 4;
    public static int wmY = 4;

    public static int winW = 315;
    public static int winH = 195;

    public static final int MIN_WIN_W = 250;
    public static final int MIN_WIN_H = 150;
    public static final int MAX_WIN_W = 600;
    public static final int MAX_WIN_H = 450;

    public static boolean fullbrightEnabled = false;

    public static boolean hitboxEnabled = false;
    public static boolean hitboxFilled = false;
    public static HitboxColorMode hitboxColorMode = HitboxColorMode.THEME;
    public static int hitboxCustomColor = 0xFFFF5555;

    public static final int[] HITBOX_PALETTE = {
            0xFFFF5555, 0xFFFF9F45, 0xFFE0D34D, 0xFF3ECF8E,
            0xFF5CC8FF, 0xFF3B6FE0, 0xFFB05CFF, 0xFFFF6BC1,
            0xFFFFFFFF, 0xFF2A2A2A,
    };

    public static int getHitboxColor() {
        return hitboxColorMode == HitboxColorMode.THEME ? getAccentColor() : hitboxCustomColor;
    }

    public static int getAccentColor() {
        return THEMES[selectedThemeIndex].color;
    }

    public static int getBgColor() {
        return darken(getAccentColor(), 0.10f, 0xF0);
    }

    public static int getHeaderColor() {
        return darken(getAccentColor(), 0.16f, 0xF0);
    }

    public static int getSideColor() {
        return darken(getAccentColor(), 0.13f, 0xF0);
    }

    public static int getChipColor() {
        return darken(getAccentColor(), 0.22f, 0xFF);
    }

    public static int getChipOnColor() {
        return darken(getAccentColor(), 0.45f, 0xFF);
    }

    public static int getTextColor() {
        return 0xFFF0F0F0;
    }

    public static int getTextDimColor() {
        return blend(getAccentColor(), 0xFFAAAAAA, 0.3f);
    }

    private static int darken(int color, float factor, int alphaByte) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int nr = Math.round(r * factor);
        int ng = Math.round(g * factor);
        int nb = Math.round(b * factor);
        return (alphaByte << 24) | (clamp(nr) << 16) | (clamp(ng) << 8) | clamp(nb);
    }

    private static int blend(int colorA, int colorB, float weightA) {
        int ar = (colorA >> 16) & 0xFF, ag = (colorA >> 8) & 0xFF, ab = colorA & 0xFF;
        int br = (colorB >> 16) & 0xFF, bg = (colorB >> 8) & 0xFF, bb = colorB & 0xFF;
        int nr = Math.round(ar * weightA + br * (1 - weightA));
        int ng = Math.round(ag * weightA + bg * (1 - weightA));
        int nb = Math.round(ab * weightA + bb * (1 - weightA));
        return 0xFF000000 | (clamp(nr) << 16) | (clamp(ng) << 8) | clamp(nb);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private SourVisualConfig() {}
}

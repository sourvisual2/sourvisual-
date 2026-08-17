package com.sourvisual.config;

public class SourVisualConfig {

    public enum Theme {
        WHITE, DARK, DARK_PLUS, PEACH, CUSTOM
    }

    public enum RgbMode {
        RADIAL, SPHERE, METRIC
    }

    // Темы
    public static Theme theme = Theme.DARK_PLUS;

    // Кастомные цвета (два свотча, как на скрине настроек)
    public static int color1R = 61,  color1G = 112, color1B = 250;
    public static int color2R = 184, color2G = 56,  color2B = 245;

    public static RgbMode rgbMode = RgbMode.METRIC;

    // Вотермарка: только ник, FPS, время
    public static boolean wmEnabled = true;
    public static int wmX = 4;
    public static int wmY = 4;

    // Размер главного окна меню (можно тянуть за правый нижний угол)
    public static int winW = 315;
    public static int winH = 195;

    public static final int MIN_WIN_W = 250;
    public static final int MIN_WIN_H = 150;
    public static final int MAX_WIN_W = 600;
    public static final int MAX_WIN_H = 450;

    // ---------- Цвета по темам ----------

    public static int getAccentColor() {
        return switch (theme) {
            case WHITE -> 0xFF3B6FE0;
            case DARK -> 0xFF7C5CFF;
            case DARK_PLUS -> 0xFF7C5CFF;
            case PEACH -> 0xFFFFB199;
            case CUSTOM -> 0xFF000000 | (color1R << 16) | (color1G << 8) | color1B;
        };
    }

    public static int getSecondaryColor() {
        return 0xFF000000 | (color2R << 16) | (color2G << 8) | color2B;
    }

    public static int getBgColor() {
        return switch (theme) {
            case WHITE -> 0xF0F2F2F5;
            case DARK -> 0xF01B1B1F;
            case DARK_PLUS -> 0xF0141414;
            case PEACH -> 0xF02B1F1B;
            case CUSTOM -> darken(color2R, color2G, color2B, 0.14f, 0xF0);
        };
    }

    public static int getHeaderColor() {
        return switch (theme) {
            case WHITE -> 0xF0E4E4E8;
            case DARK -> 0xF0242429;
            case DARK_PLUS -> 0xF01B1B1B;
            case PEACH -> 0xF03A2A24;
            case CUSTOM -> darken(color2R, color2G, color2B, 0.22f, 0xF0);
        };
    }

    public static int getSideColor() {
        return switch (theme) {
            case WHITE -> 0xF0E9E9ED;
            case DARK -> 0xF0202024;
            case DARK_PLUS -> 0xF0181818;
            case PEACH -> 0xF0332521;
            case CUSTOM -> darken(color2R, color2G, color2B, 0.18f, 0xF0);
        };
    }

    public static int getTextColor() {
        return switch (theme) {
            case WHITE -> 0xFF1C1C1C;
            case DARK -> 0xFFE0E0E0;
            case DARK_PLUS -> 0xFFE0E0E0;
            case PEACH -> 0xFFFCEAE0;
            case CUSTOM -> 0xFFF0F0F0;
        };
    }

    public static int getTextDimColor() {
        return switch (theme) {
            case WHITE -> 0xFF6E6E6E;
            case DARK -> 0xFF8A8A8A;
            case DARK_PLUS -> 0xFF8A8A8A;
            case PEACH -> 0xFFC9A79A;
            case CUSTOM -> 0xFFAAAAAA;
        };
    }

    public static int getChipColor() {
        return switch (theme) {
            case WHITE -> 0xFFD5D5DA;
            case DARK -> 0xFF2A2A2F;
            case DARK_PLUS -> 0xFF232323;
            case PEACH -> 0xFF3F2E28;
            case CUSTOM -> darken(color2R, color2G, color2B, 0.30f, 0xFF);
        };
    }

    public static int getChipOnColor() {
        int acc = getAccentColor();
        int r = (acc >> 16) & 0xFF;
        int g = (acc >> 8) & 0xFF;
        int b = acc & 0xFF;
        return darken(r, g, b, 0.55f, 0xFF);
    }

    private static int darken(int r, int g, int b, float factor, int alphaByte) {
        int nr = Math.round(r * factor);
        int ng = Math.round(g * factor);
        int nb = Math.round(b * factor);
        return (alphaByte << 24) | (clamp(nr) << 16) | (clamp(ng) << 8) | clamp(nb);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private SourVisualConfig() {}
        }

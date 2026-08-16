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

    // Вотермарка: только ник, FPS, время — общий тумблер видимости
    public static boolean wmEnabled = true;

    // Позиция вотермарки (перетаскивается мышкой при открытом меню)
    public static int wmX = 4;
    public static int wmY = 4;

    public static int getAccentColor() {
        return switch (theme) {
            case WHITE -> 0xFFE0E0E0;
            case DARK -> 0xFF7C5CFF;
            case DARK_PLUS -> 0xFF7C5CFF;
            case PEACH -> 0xFFFFB199;
            case CUSTOM -> 0xFF000000 | (color1R << 16) | (color1G << 8) | color1B;
        };
    }

    public static int getSecondaryColor() {
        return 0xFF000000 | (color2R << 16) | (color2G << 8) | color2B;
    }

    private SourVisualConfig() {}
}

package com.sourvisual.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class SourVisualConfig {

    public static class ThemePreset {
        public final String name;
        public final int color;

        public ThemePreset(String name, int color) {
            this.name = name;
            this.color = color;
        }
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

    // Позиция меню. Integer.MIN_VALUE = ещё не задана (первое открытие центрируется).
    public static int menuX = Integer.MIN_VALUE;
    public static int menuY = Integer.MIN_VALUE;

    public static int winW = 315;
    public static int winH = 195;

    public static final int MIN_WIN_W = 250;
    public static final int MIN_WIN_H = 150;
    public static final int MAX_WIN_W = 600;
    public static final int MAX_WIN_H = 450;

    public static boolean fullbrightEnabled = false;

    // ---------- Сохранение / загрузка ----------

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("sourvisual.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static class Data {
        int selectedThemeIndex;
        boolean wmEnabled;
        int wmX, wmY;
        int menuX = Integer.MIN_VALUE, menuY = Integer.MIN_VALUE;
        int winW, winH;
        boolean fullbrightEnabled;
    }

    public static void load() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                return;
            }
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                Data data = GSON.fromJson(reader, Data.class);
                if (data == null) {
                    return;
                }
                if (data.selectedThemeIndex >= 0 && data.selectedThemeIndex < THEMES.length) {
                    selectedThemeIndex = data.selectedThemeIndex;
                }
                wmEnabled = data.wmEnabled;
                wmX = data.wmX;
                wmY = data.wmY;
                menuX = data.menuX;
                menuY = data.menuY;
                if (data.winW >= MIN_WIN_W && data.winW <= MAX_WIN_W) {
                    winW = data.winW;
                }
                if (data.winH >= MIN_WIN_H && data.winH <= MAX_WIN_H) {
                    winH = data.winH;
                }
                fullbrightEnabled = data.fullbrightEnabled;
            }
        } catch (IOException | RuntimeException e) {
            // повреждённый или отсутствующий конфиг — остаёмся на значениях по умолчанию
        }
    }

    public static void save() {
        try {
            Data data = new Data();
            data.selectedThemeIndex = selectedThemeIndex;
            data.wmEnabled = wmEnabled;
            data.wmX = wmX;
            data.wmY = wmY;
            data.menuX = menuX;
            data.menuY = menuY;
            data.winW = winW;
            data.winH = winH;
            data.fullbrightEnabled = fullbrightEnabled;

            Path parent = CONFIG_PATH.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            // сохранение — best effort, ошибку молча игнорируем
        }
    }

    // ---------- Цвета интерфейса, производные от выбранной темы ----------

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

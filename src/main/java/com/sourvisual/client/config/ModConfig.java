package com.sourvisual.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;

public class ModConfig {

    public static boolean hitColorEnabled = false;
    // цвет в формате RGB int (по умолчанию фиолетовый)
    public static int hitColorR = 123;
    public static int hitColorG = 0;
    public static int hitColorB = 255;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("sourvisual.json");

    // для сериализации
    public boolean hitColorEnabled_s = false;
    public int hitColorR_s = 123;
    public int hitColorG_s = 0;
    public int hitColorB_s = 255;

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                Reader reader = Files.newBufferedReader(CONFIG_PATH);
                ModConfig data = GSON.fromJson(reader, ModConfig.class);
                reader.close();
                if (data != null) {
                    hitColorEnabled = data.hitColorEnabled_s;
                    hitColorR       = data.hitColorR_s;
                    hitColorG       = data.hitColorG_s;
                    hitColorB       = data.hitColorB_s;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void save() {
        try {
            ModConfig data = new ModConfig();
            data.hitColorEnabled_s = hitColorEnabled;
            data.hitColorR_s       = hitColorR;
            data.hitColorG_s       = hitColorG;
            data.hitColorB_s       = hitColorB;
            Writer writer = Files.newBufferedWriter(CONFIG_PATH);
            GSON.toJson(data, writer);
            writer.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Возвращает ARGB int для рендерера
    public static int getHitColor() {
        return (255 << 24) | (hitColorR << 16) | (hitColorG << 8) | hitColorB;
    }
}

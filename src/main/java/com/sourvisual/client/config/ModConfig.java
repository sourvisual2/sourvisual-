package com.sourvisual.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;

public class ModConfig {

    public static boolean hitColorEnabled = false;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("sourvisual.json");

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                Reader reader = Files.newBufferedReader(CONFIG_PATH);
                ModConfig data = GSON.fromJson(reader, ModConfig.class);
                reader.close();
                if (data != null) {
                    hitColorEnabled = data.hitColorEnabled;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            ModConfig data = new ModConfig();
            data.hitColorEnabled = ModConfig.hitColorEnabled;
            Writer writer = Files.newBufferedWriter(CONFIG_PATH);
            GSON.toJson(data, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

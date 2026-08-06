package com.sourvisual;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SourVisual implements ModInitializer {

    public static final String MOD_ID = "sourvisual";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("SourVisual 1.21 loaded!");
    }
}

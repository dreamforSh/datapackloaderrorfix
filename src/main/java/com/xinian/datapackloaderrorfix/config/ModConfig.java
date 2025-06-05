package com.xinian.datapackloaderrorfix.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModConfig {
    private static final Logger LOGGER = LogManager.getLogger();


    public static boolean cleanDimensions = true;
    public static boolean cleanEntities = true;
    public static boolean cleanChunks = true;
    public static boolean cleanDatapacks = true;
    public static boolean cleanPlayerInventory = true;
    public static boolean cleanTileEntities = true;


    public static boolean createBackups = true;
    public static boolean enableDetailedLogging = false;

    public static void init() {
        LOGGER.info("加载模组配置...");

        validateConfig();
    }

    private static void validateConfig() {

        if (!cleanDimensions && !cleanEntities && !cleanChunks && !cleanDatapacks) {
            LOGGER.warn("所有清理功能都被禁用，模组将不会执行任何操作");
        }
    }

    public static boolean shouldCreateBackups() {
        return createBackups;
    }

    public static boolean isCleaningEnabled() {
        return cleanDimensions || cleanEntities || cleanChunks || cleanDatapacks;
    }
}

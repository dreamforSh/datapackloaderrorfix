package com.xinian.datapackloaderrorfix;

import com.xinian.datapackloaderrorfix.config.ModConfig;
import com.xinian.datapackloaderrorfix.handler.LevelLoadHandler;
import com.xinian.datapackloaderrorfix.util.ModDetector;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Main.MOD_ID)
public class Main {
    public static final String MOD_ID = "datapackloaderrorfix";
    private static final Logger LOGGER = LogManager.getLogger();

    public Main() {
        LOGGER.info("Datapack Load Error Fix 初始化中...");


        ModConfig.init();

        MinecraftForge.EVENT_BUS.register(LevelLoadHandler.class);

        LOGGER.info("Datapack Load Error Fix 初始化完成");
    }

    public static boolean isModLoaded(String modid) {
        return ModDetector.isModLoaded(modid);
    }
}

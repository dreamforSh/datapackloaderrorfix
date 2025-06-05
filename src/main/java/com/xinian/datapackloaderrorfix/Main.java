package com.xinian.datapackloaderrorfix;

import com.xinian.datapackloaderrorfix.config.ModConfig;

import com.xinian.datapackloaderrorfix.handler.GuiScreenEventHandler;
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
        LOGGER.info("status=initializing, mod={}, action=init_start", MOD_ID);

        ModConfig.init();


        MinecraftForge.EVENT_BUS.register(GuiScreenEventHandler.class);

        LOGGER.info("status=completed, mod={}, action=init_finished", MOD_ID);
    }

    public static boolean isModLoaded(String modid) {
        return ModDetector.isModLoaded(modid);
    }
}
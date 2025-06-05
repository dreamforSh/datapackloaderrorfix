package com.xinian.datapackloaderrorfix.handler;

import com.xinian.datapackloaderrorfix.Main;
import com.xinian.datapackloaderrorfix.config.ModConfig;
import com.xinian.datapackloaderrorfix.processor.WorldDataProcessor;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = Main.MOD_ID)
public class LevelLoadHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean hasProcessed = false;

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide() || hasProcessed || !ModConfig.isCleaningEnabled()) {
            return;
        }

        hasProcessed = true;
        LOGGER.info("检测到世界加载事件，开始处理世界数据...");

        try {
            Minecraft minecraft = Minecraft.getInstance();
            WorldDataProcessor processor = new WorldDataProcessor();
            processor.processCurrentWorld(minecraft);
        } catch (Exception e) {
            LOGGER.error("处理世界数据时出错", e);
        } finally {

            Runtime.getRuntime().addShutdownHook(new Thread(() -> hasProcessed = false));
        }
    }
}


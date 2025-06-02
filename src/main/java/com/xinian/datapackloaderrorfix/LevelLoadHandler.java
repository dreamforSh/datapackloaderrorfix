package com.xinian.datapackloaderrorfix;

import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.storage.LevelStorageSource;
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
        if (event.getLevel().isClientSide() && !hasProcessed) {
            hasProcessed = true;

            try {
                Minecraft minecraft = Minecraft.getInstance();
                processCurrentWorld(minecraft);
            } catch (Exception e) {
                LOGGER.error("处理世界数据时出错", e);
            }
        }
    }

    private static void processCurrentWorld(Minecraft minecraft) {
        try {
            LevelStorageSource levelSource = minecraft.getLevelSource();
            String levelId = null;

            if (minecraft.getCurrentServer() != null) {
                levelId = minecraft.getCurrentServer().name;
            } else if (minecraft.getSingleplayerServer() != null) {
                levelId = minecraft.getSingleplayerServer().getWorldData().getLevelName();
            }

            if (levelId == null) {
                LOGGER.warn("无法确定当前世界名称，跳过处理");
                return;
            }

            LOGGER.info("正在处理世界: {}", levelId);
            LevelStorageSource.LevelStorageAccess levelAccess = levelSource.createAccess(levelId);
            File worldDir = levelAccess.getWorldDir().toFile();


            File levelFile = new File(worldDir, "level.dat");
            LOGGER.info("正在检查并修复level.dat文件中的数据...");
            Fix.fixLevelDat(levelFile);


            if (Main.cleanChunks) {
                LOGGER.info("正在检查并修复区块数据中的实体引用...");
                Fix.fixChunks(worldDir);
            }


            if (Main.cleanDatapacks) {
                LOGGER.info("正在检查并修复数据包配置...");
                DatapackFixer.fixDatapacks(worldDir);
            }

            levelAccess.close();
        } catch (Exception e) {
            LOGGER.error("处理世界数据时出错", e);
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> hasProcessed = false));
        }
    }


}

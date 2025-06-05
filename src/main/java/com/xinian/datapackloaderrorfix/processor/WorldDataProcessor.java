package com.xinian.datapackloaderrorfix.processor;

import com.xinian.datapackloaderrorfix.config.ModConfig;
import com.xinian.datapackloaderrorfix.processor.impl.ChunkDataProcessor;
import com.xinian.datapackloaderrorfix.processor.impl.DatapackProcessor;
import com.xinian.datapackloaderrorfix.processor.impl.LevelDataProcessor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class WorldDataProcessor {
    private static final Logger LOGGER = LogManager.getLogger();

    private final LevelDataProcessor levelProcessor;
    private final ChunkDataProcessor chunkProcessor;
    private final DatapackProcessor datapackProcessor;

    public WorldDataProcessor() {
        this.levelProcessor = new LevelDataProcessor();
        this.chunkProcessor = new ChunkDataProcessor();
        this.datapackProcessor = new DatapackProcessor();
    }

    public void processCurrentWorld(Minecraft minecraft) {
        try {
            File worldDir = getWorldDirectory(minecraft);
            if (worldDir == null) {
                LOGGER.warn("无法获取世界目录，跳过处理");
                return;
            }

            LOGGER.info("开始处理世界: {}", worldDir.getName());


            if (ModConfig.cleanDimensions || ModConfig.cleanEntities) {
                File levelFile = new File(worldDir, "level.dat");
                if (levelFile.exists()) {
                    levelProcessor.processLevelData(levelFile);
                }
            }


            if (ModConfig.cleanChunks) {
                chunkProcessor.processChunkData(worldDir);
            }


            if (ModConfig.cleanDatapacks) {
                datapackProcessor.processDatapacks(worldDir);
            }

            LOGGER.info("世界数据处理完成");

        } catch (Exception e) {
            LOGGER.error("处理世界数据时出错", e);
        }
    }

    private File getWorldDirectory(Minecraft minecraft) {
        try {
            LevelStorageSource levelSource = minecraft.getLevelSource();
            String levelId = determineLevelId(minecraft);

            if (levelId == null) {
                return null;
            }

            LevelStorageSource.LevelStorageAccess levelAccess = levelSource.createAccess(levelId);
            File worldDir = levelAccess.getWorldDir().toFile();
            levelAccess.close();

            return worldDir;
        } catch (Exception e) {
            LOGGER.error("获取世界目录失败", e);
            return null;
        }
    }

    private String determineLevelId(Minecraft minecraft) {
        if (minecraft.getCurrentServer() != null) {
            return minecraft.getCurrentServer().name;
        } else if (minecraft.getSingleplayerServer() != null) {
            return minecraft.getSingleplayerServer().getWorldData().getLevelName();
        }
        return null;
    }
}

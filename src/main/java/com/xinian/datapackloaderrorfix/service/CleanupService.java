package com.xinian.datapackloaderrorfix.service;

import com.xinian.datapackloaderrorfix.config.ModConfig;
import com.xinian.datapackloaderrorfix.processor.impl.ChunkDataProcessor;
import com.xinian.datapackloaderrorfix.processor.impl.DatapackProcessor;
import com.xinian.datapackloaderrorfix.processor.impl.LevelDataProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CleanupService {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ExecutorService executorService;

    private final LevelDataProcessor levelProcessor;
    private final ChunkDataProcessor chunkProcessor;
    private final DatapackProcessor datapackProcessor;

    public CleanupService() {
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r, "DatapackFixer-Worker");
            thread.setDaemon(true);
            return thread;
        });

        this.levelProcessor = new LevelDataProcessor();
        this.chunkProcessor = new ChunkDataProcessor();
        this.datapackProcessor = new DatapackProcessor();
    }

    public CompletableFuture<Void> cleanupWorldAsync(File worldDir) {
        return CompletableFuture.runAsync(() -> cleanupWorld(worldDir), executorService);
    }

    public void cleanupWorld(File worldDir) {
        LOGGER.info("开始清理世界数据: {}", worldDir.getName());

        try {
            // 清理level.dat
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

            LOGGER.info("世界数据清理完成: {}", worldDir.getName());

        } catch (Exception e) {
            LOGGER.error("清理世界数据时出错: {}", worldDir.getName(), e);
            throw new RuntimeException("清理失败", e);
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }
}

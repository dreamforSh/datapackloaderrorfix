package com.xinian.datapackloaderrorfix.processor.impl;

import com.xinian.datapackloaderrorfix.processor.BaseProcessor;
import com.xinian.datapackloaderrorfix.util.BackupManager;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class ChunkDataProcessor extends BaseProcessor {

    public ChunkDataProcessor() {
        super(LogManager.getLogger());
    }

    public void processChunkData(File worldDir) {
        logProcessStart("区块数据", worldDir.getName());

        try {

            processDimensionChunks(new File(worldDir, "region"), "主世界");


            processDimensionChunks(new File(worldDir, "DIM-1/region"), "下界");


            processDimensionChunks(new File(worldDir, "DIM1/region"), "末地");


            processCustomDimensions(worldDir);

            logProcessComplete("区块数据", worldDir.getName(), true);

        } catch (Exception e) {
            logError("处理区块数据", worldDir.getName(), e);
        }
    }

    private void processDimensionChunks(File regionDir, String dimensionName) {
        if (!regionDir.exists() || !regionDir.isDirectory()) {
            logger.debug("维度区块目录不存在: {} ({})", regionDir.getPath(), dimensionName);
            return;
        }

        logger.info("正在处理{}区块目录: {}", dimensionName, regionDir.getPath());

        File[] regionFiles = regionDir.listFiles((dir, name) -> name.endsWith(".mca"));
        if (regionFiles == null || regionFiles.length == 0) {
            logger.debug("{}没有找到区块文件", dimensionName);
            return;
        }

        for (File regionFile : regionFiles) {
            try {
                processRegionFile(regionFile);
            } catch (Exception e) {
                logError("处理区块文件", regionFile.getName(), e);
            }
        }
    }

    private void processRegionFile(File regionFile) {
        if (!BackupManager.createBackup(regionFile)) {
            logger.warn("无法创建备份，跳过区块文件: {}", regionFile.getName());
            return;
        }

        logger.info("处理区块文件: {}", regionFile.getName());

        // TODO: 实现具体的区块文件处理逻辑


        try {
            cleanRegionFile(regionFile);
        } catch (Exception e) {
            logError("清理区块文件", regionFile.getName(), e);
        }
    }

    private void cleanRegionFile(File regionFile) {
        // TODO: 实现区块文件清理逻辑
        logger.debug("区块文件清理功能待实现: {}", regionFile.getName());
    }

    private void processCustomDimensions(File worldDir) {
        File dimensionsDir = new File(worldDir, "dimensions");
        if (!dimensionsDir.exists() || !dimensionsDir.isDirectory()) {
            return;
        }

        try (Stream<Path> paths = Files.walk(dimensionsDir.toPath())) {
            List<File> regionDirs = paths
                    .filter(path -> path.toFile().isDirectory())
                    .filter(path -> "region".equals(path.getFileName().toString()))
                    .map(Path::toFile)
                    .toList();

            for (File regionDir : regionDirs) {
                String customDimName = regionDir.getParentFile().getName();
                processDimensionChunks(regionDir, "自定义维度:" + customDimName);
            }
        } catch (IOException e) {
            logError("处理自定义维度", dimensionsDir.getName(), e);
        }
    }
}
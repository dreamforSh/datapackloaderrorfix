package com.xinian.datapackloaderrorfix.util;

import com.xinian.datapackloaderrorfix.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static boolean createBackup(File originalFile) {
        if (!ModConfig.shouldCreateBackups()) {
            return true;
        }

        try {
            File backupFile = generateBackupFile(originalFile);
            if (!backupFile.exists()) {
                Files.copy(originalFile.toPath(), backupFile.toPath());
                LOGGER.info("已创建备份文件: {}", backupFile.getName());
                return true;
            } else {
                LOGGER.debug("备份文件已存在: {}", backupFile.getName());
                return true;
            }
        } catch (IOException e) {
            LOGGER.error("创建备份文件失败: {}", originalFile.getName(), e);
            return false;
        }
    }

    private static File generateBackupFile(File originalFile) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String backupName = originalFile.getName() + ".bak_" + timestamp;
        return new File(originalFile.getParentFile(), backupName);
    }

    public static File getSimpleBackupFile(File originalFile) {
        String backupName = originalFile.getName() + ".bak";
        return new File(originalFile.getParentFile(), backupName);
    }
}

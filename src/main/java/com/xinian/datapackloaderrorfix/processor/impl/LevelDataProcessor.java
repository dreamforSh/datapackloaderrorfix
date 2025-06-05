package com.xinian.datapackloaderrorfix.processor.impl;

import com.xinian.datapackloaderrorfix.config.ModConfig;
import com.xinian.datapackloaderrorfix.processor.BaseProcessor;
import com.xinian.datapackloaderrorfix.util.BackupManager;
import com.xinian.datapackloaderrorfix.util.ModDetector;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LevelDataProcessor extends BaseProcessor {

    public LevelDataProcessor() {
        super(LogManager.getLogger());
    }

    public void processLevelData(File levelFile) {
        logProcessStart("Level数据", levelFile.getName());

        try {
            if (!BackupManager.createBackup(levelFile)) {
                logger.warn("无法创建备份，跳过处理: {}", levelFile.getName());
                return;
            }

            CompoundTag root = NbtIo.readCompressed(levelFile);
            CompoundTag data = root.getCompound("Data");
            boolean hasChanges = false;

            // 清理维度数据
            if (ModConfig.cleanDimensions) {
                hasChanges |= cleanDimensions(data);
            }

            // 清理实体数据
            if (ModConfig.cleanEntities) {
                hasChanges |= cleanPlayerData(data);
            }

            if (hasChanges) {
                NbtIo.writeCompressed(root, levelFile);
            }

            logProcessComplete("Level数据", levelFile.getName(), hasChanges);

        } catch (IOException e) {
            logError("处理Level数据", levelFile.getName(), e);
        }
    }

    private boolean cleanDimensions(CompoundTag data) {
        CompoundTag worldGenSettings = data.getCompound("WorldGenSettings");
        if (!worldGenSettings.contains("dimensions")) {
            return false;
        }

        CompoundTag dimensions = worldGenSettings.getCompound("dimensions");
        Set<String> keys = dimensions.getAllKeys();
        List<String> toRemove = new ArrayList<>();

        for (String key : keys) {
            String modId = ModDetector.extractModId(key);
            if (modId != null && !ModDetector.isModLoaded(modId)) {
                toRemove.add(key);
            }
        }

        boolean changed = false;
        for (String key : toRemove) {
            dimensions.remove(key);
            logger.info("已移除残留的维度数据: {}", key);
            changed = true;
        }

        return changed;
    }

    private boolean cleanPlayerData(CompoundTag data) {
        boolean changed = false;

        if (data.contains("Player")) {
            CompoundTag player = data.getCompound("Player");

            if (ModConfig.cleanPlayerInventory) {
                if (player.contains("Inventory")) {
                    changed |= cleanInventory(player.getList("Inventory", 10), "背包");
                }

                if (player.contains("EnderItems")) {
                    changed |= cleanInventory(player.getList("EnderItems", 10), "末影箱");
                }
            }
        }

        return changed;
    }

    private boolean cleanInventory(ListTag inventory, String inventoryType) {
        List<Integer> toRemove = new ArrayList<>();

        for (int i = 0; i < inventory.size(); i++) {
            CompoundTag item = inventory.getCompound(i);
            if (item.contains("id")) {
                String id = item.getString("id");
                String modId = ModDetector.extractModId(id);

                if (modId != null && !ModDetector.isModLoaded(modId)) {
                    toRemove.add(i);
                    logger.info("发现{}中引用不存在模组的物品: {}", inventoryType, id);
                }
            }
        }

        // 从后往前删除以避免索引问题
        for (int i = toRemove.size() - 1; i >= 0; i--) {
            int index = toRemove.get(i);
            CompoundTag item = inventory.getCompound(index);
            String id = item.getString("id");
            inventory.remove(index);
            logger.info("已从{}移除残留物品: {}", inventoryType, id);
        }

        return !toRemove.isEmpty();
    }
}
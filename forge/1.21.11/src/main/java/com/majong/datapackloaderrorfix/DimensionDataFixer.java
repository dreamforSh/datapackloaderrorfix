package com.majong.datapackloaderrorfix;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.majong.datapackloaderrorfix.common.DimensionFix;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;

public class DimensionDataFixer {
    private static final Logger LOGGER = LogManager.getLogger();
    
    public static void fixLevelData(File levelFile) {
        try {
            boolean modified = false;
            Path levelPath = levelFile.toPath();
            CompoundTag rootTag = NbtIo.readCompressed(levelPath, NbtAccounter.unlimitedHeap());
            CompoundTag dataTag = rootTag.getCompound("Data").orElseThrow(() -> new IOException("Missing Data tag"));
            CompoundTag worldGenSettings = dataTag.getCompound("WorldGenSettings").orElseThrow(() -> new IOException("Missing WorldGenSettings tag"));
            CompoundTag dimensionsTag = worldGenSettings.getCompound("dimensions").orElseThrow(() -> new IOException("Missing dimensions tag"));
            Set<String> dimensionKeys = dimensionsTag.keySet();
            
            List<String> removedDimensions = DimensionFix.findRemovedDimensions(dimensionKeys, DatapackLoadErrorFix::isModLoaded);
            
            for (String dimensionKey : removedDimensions) {
                dimensionsTag.remove(dimensionKey);
                LOGGER.info("Removed orphaned dimension data: {}", dimensionKey);
                modified = true;
            }
            
            if (modified) {
                NbtIo.writeCompressed(rootTag, levelPath);
            } else {
                LOGGER.info("No orphaned dimension data detected");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to fix dimension data", e);
        }
    }
}

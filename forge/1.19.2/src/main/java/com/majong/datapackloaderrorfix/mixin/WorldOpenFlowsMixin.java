package com.majong.datapackloaderrorfix.mixin;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.majong.datapackloaderrorfix.DimensionDataFixer;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;

@Mixin(WorldOpenFlows.class)
public class WorldOpenFlowsMixin {
    private static final Logger LOGGER = LogManager.getLogger();
    
    @Shadow
    private LevelStorageSource levelSource;
    
    @Inject(at = @At("HEAD"), method = "doLoadLevel")
    private void onDoLoadLevel(Screen screen, String worldName, boolean create, boolean backup, CallbackInfo ci) {
        try {
            LevelStorageSource.LevelStorageAccess levelAccess = this.levelSource.createAccess(worldName);
            File levelDataFile = levelAccess.getLevelPath(LevelResource.LEVEL_DATA_FILE).toFile();
            levelAccess.close();
            DimensionDataFixer.fixLevelData(levelDataFile);
        } catch (IOException e) {
            LOGGER.error("Failed to fix dimension data during world open", e);
        }
    }
}

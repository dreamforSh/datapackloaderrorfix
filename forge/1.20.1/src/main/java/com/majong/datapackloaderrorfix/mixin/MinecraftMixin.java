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

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.storage.LevelStorageSource;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    private static final Logger LOGGER = LogManager.getLogger();
    
    @Shadow
    private LevelStorageSource levelSource;
    
    @Inject(at = @At("HEAD"), method = "loadLevel")
    private void onLoadLevel(String worldName, CallbackInfo ci) {
        try {
            LevelStorageSource.LevelStorageAccess levelAccess = this.levelSource.createAccess(worldName);
            File worldDirectory = levelAccess.getWorldDir().toFile();
            File levelDataFile = new File(worldDirectory, "level.dat");
            levelAccess.close();
            DimensionDataFixer.fixLevelData(levelDataFile);
        } catch (IOException e) {
            LOGGER.error("Failed to fix dimension data during level load", e);
        }
    }
}

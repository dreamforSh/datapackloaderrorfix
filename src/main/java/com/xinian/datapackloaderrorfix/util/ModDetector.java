package com.xinian.datapackloaderrorfix.util;

import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class ModDetector {
    private static final Logger LOGGER = LogManager.getLogger();
    private static Set<String> loadedMods = null;

    public static boolean isModLoaded(String modid) {
        if (loadedMods == null) {
            initializeModList();
        }
        return loadedMods.contains(modid);
    }

    private static void initializeModList() {
        loadedMods = new HashSet<>();
        for (ModInfo info : FMLLoader.getLoadingModList().getMods()) {
            loadedMods.add(info.getModId());
        }
        LOGGER.debug("已加载 {} 个模组", loadedMods.size());
    }

    public static Set<String> getLoadedMods() {
        if (loadedMods == null) {
            initializeModList();
        }
        return new HashSet<>(loadedMods);
    }

    public static String extractModId(String reference) {
        if (reference == null || !reference.contains(":")) {
            return null;
        }
        int colonIndex = reference.indexOf(':');
        return colonIndex > 0 ? reference.substring(0, colonIndex) : null;
    }

    public static boolean isModReference(String str) {
        return str != null && str.contains(":") && extractModId(str) != null;
    }
}

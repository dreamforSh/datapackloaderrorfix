package com.xinian.datapackloaderrorfix;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Main.MOD_ID)
public class Main {
	public static final String MOD_ID = "datapackloaderrorfix";
	private static final Logger LOGGER = LogManager.getLogger();


	public static boolean cleanDimensions = true;
	public static boolean cleanEntities = true;
	public static boolean cleanChunks = true;
	public static boolean cleanDatapacks = true;
	public static boolean createBackups = false;

	public Main() {
		LOGGER.info("Datapack Load Error Fix 初始化中...");
		MinecraftForge.EVENT_BUS.register(LevelLoadHandler.class);
		LOGGER.info("Datapack Load Error Fix 初始化完成");
	}

	public static boolean isModLoaded(String modid) {
		for(ModInfo info : FMLLoader.getLoadingModList().getMods()){
			if(info.getModId().equals(modid))
				return true;
		}
		return false;
	}
}


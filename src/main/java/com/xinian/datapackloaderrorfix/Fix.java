package com.xinian.datapackloaderrorfix;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;

public class Fix {
	private static final Logger LOGGER = LogManager.getLogger();


	public static void fixLevelDat(File levelFile) {
		try {

			if (Main.createBackups) {
					File backup = new File(levelFile.getParentFile(), "level.dat.bak");
					if (!backup.exists()) {
						LOGGER.info("创建level.dat备份...");
						Files.copy(levelFile.toPath(), backup.toPath());
					}
				}

				boolean changed = false;
				CompoundTag root = NbtIo.readCompressed(levelFile);
				CompoundTag data = root.getCompound("Data");

			if (Main.cleanDimensions) {
				CompoundTag worldGenSettings = data.getCompound("WorldGenSettings");
				if (worldGenSettings.contains("dimensions")) {
					changed |= cleanDimensions(worldGenSettings.getCompound("dimensions"));
				}
			}


			if (Main.cleanEntities) {
				changed |= cleanEntities(data);
			}

			if (changed) {
				NbtIo.writeCompressed(root, levelFile);
				LOGGER.info("已保存对level.dat的修改");
			} else {
				LOGGER.info("未检测到需要清理的数据");
			}
		} catch (IOException e) {
			LOGGER.error("处理level.dat文件时出错", e);
		}
	}

	/**
	 * 清理维度数据
	 */
	private static boolean cleanDimensions(CompoundTag dimensions) {
		boolean changed = false;
		Set<String> keys = dimensions.getAllKeys();
		List<String> removedKeys = new ArrayList<>();

		for (String key : keys) {
			int colonIndex = key.indexOf(':');
			if (colonIndex > 0) {
				String modId = key.substring(0, colonIndex);
				if (!Main.isModLoaded(modId)) {
					removedKeys.add(key);
				}
			}
		}

		for (String key : removedKeys) {
			dimensions.remove(key);
			LOGGER.info("已移除残留的维度数据：{}", key);
			changed = true;
		}

		return changed;
	}

	/**
	 * 清理实体数据
	 */
	private static boolean cleanEntities(CompoundTag data) {
		boolean changed = false;

		// 清理玩家数据
		if (data.contains("Player")) {
			CompoundTag player = data.getCompound("Player");

			// 清理物品栏
			if (player.contains("Inventory")) {
				changed |= cleanInventory(player.getList("Inventory", 10));
			}

			// 清理末影箱
			if (player.contains("EnderItems")) {
				changed |= cleanInventory(player.getList("EnderItems", 10));
			}
		}

		return changed;
	}


	private static boolean cleanInventory(ListTag inventory) {
		boolean changed = false;
		List<Integer> toRemove = new ArrayList<>();

		for (int i = 0; i < inventory.size(); i++) {
			CompoundTag item = inventory.getCompound(i);
			if (item.contains("id")) {
				String id = item.getString("id");
				int colonIndex = id.indexOf(':');
				if (colonIndex > 0) {
					String modId = id.substring(0, colonIndex);
					if (!Main.isModLoaded(modId)) {
						toRemove.add(i);
						LOGGER.info("发现引用不存在模组的物品: {}", id);
					}
				}
			}
		}


		for (int i = toRemove.size() - 1; i >= 0; i--) {
			int index = toRemove.get(i);
			CompoundTag item = inventory.getCompound(index);
			String id = item.getString("id");
			inventory.remove(index);
			LOGGER.info("已移除残留的物品：{}", id);
			changed = true;
		}

		return changed;
	}


	public static void fixChunks(File worldDir) {
		try {

			processDimensionChunks(new File(worldDir, "region"));
			processDimensionChunks(new File(worldDir, "DIM-1/region")); // 下界
			processDimensionChunks(new File(worldDir, "DIM1/region"));  // 末地


			File dimensionsDir = new File(worldDir, "dimensions");
			if (dimensionsDir.exists() && dimensionsDir.isDirectory()) {
				processCustomDimensions(dimensionsDir);
			}
		} catch (Exception e) {
			LOGGER.error("处理区块数据时出错", e);
		}
	}


	private static void processDimensionChunks(File regionDir) {
		if (!regionDir.exists() || !regionDir.isDirectory()) {
			return;
		}

		LOGGER.info("正在处理区块目录: {}", regionDir.getPath());
		File[] regionFiles = regionDir.listFiles((dir, name) -> name.endsWith(".mca"));

		if (regionFiles != null) {
			for (File regionFile : regionFiles) {
				try {
					// 创建备份
					if (Main.createBackups) {
						File backup = new File(regionFile.getParentFile(), regionFile.getName() + ".bak");
						if (!backup.exists()) {
							Files.copy(regionFile.toPath(), backup.toPath());
						}
					}

					// 这里需要实现区块文件的处理逻辑
					// 由于区块文件格式较复杂，这里只提供一个框架
					// 实际实现需要使用专门的区块解析库或参考Minecraft源码
					LOGGER.info("处理区块文件: {}", regionFile.getName());
					cleanRegionFile(regionFile);

				} catch (Exception e) {
					LOGGER.error("处理区块文件时出错: {}", regionFile.getName(), e);
				}
			}
		}
	}

	/**
	 * 处理自定义维度的区块数据
	 */
	private static void processCustomDimensions(File dimensionsDir) {
		try {
			// 遍历所有自定义维度
			try (Stream<Path> paths = Files.walk(dimensionsDir.toPath())) {
				List<File> regionDirs = paths
						.filter(path -> path.toFile().isDirectory())
						.filter(path -> path.getFileName().toString().equals("region"))
						.map(Path::toFile)
						.toList();

				for (File regionDir : regionDirs) {
					processDimensionChunks(regionDir);
				}
			}
		} catch (IOException e) {
			LOGGER.error("处理自定义维度时出错", e);
		}
	}

	/**
	 * 清理区块文件中的实体数据
	 * 注意：这是一个框架方法，实际实现需要根据Minecraft的区块格式进行开发
	 */
	private static void cleanRegionFile(File regionFile) {
		// 这里需要实现区块文件的处理逻辑
		// 由于区块文件格式较复杂，需要使用专门的区块解析库或参考Minecraft源码

		// 示例伪代码:
        /*
        RegionFile region = new RegionFile(regionFile.toPath(), regionFile.getParentFile().toPath(), false);

        for (int x = 0; x < 32; x++) {
            for (int z = 0; z < 32; z++) {
                if (region.hasChunk(new ChunkPos(x, z))) {
                    CompoundTag chunkData = region.getChunk(new ChunkPos(x, z));
                    if (chunkData != null) {
                        boolean changed = false;

                        // 清理实体列表
                        if (chunkData.contains("Entities", 9)) {
                            ListTag entities = chunkData.getList("Entities", 10);
                            changed |= cleanEntitiesList(entities);
                        }

                        // 清理方块实体列表
                        if (chunkData.contains("TileEntities", 9)) {
                            ListTag tileEntities = chunkData.getList("TileEntities", 10);
                            changed |= cleanTileEntitiesList(tileEntities);
                        }

                        if (changed) {
                            region.write(new ChunkPos(x, z), chunkData);
                        }
                    }
                }
            }
        }

        region.close();
        */


		LOGGER.info("区块文件处理框架已准备，但需要实际实现");
	}


	private static boolean cleanEntitiesList(ListTag entities) {
		boolean changed = false;
		List<Integer> toRemove = new ArrayList<>();

		for (int i = 0; i < entities.size(); i++) {
			CompoundTag entity = entities.getCompound(i);
			if (entity.contains("id")) {
				String id = entity.getString("id");
				int colonIndex = id.indexOf(':');
				if (colonIndex > 0) {
					String modId = id.substring(0, colonIndex);
					if (!Main.isModLoaded(modId)) {
						toRemove.add(i);
						LOGGER.info("发现引用不存在模组的实体: {}", id);
					}
				}
			}
		}


		for (int i = toRemove.size() - 1; i >= 0; i--) {
			int index = toRemove.get(i);
			CompoundTag entity = entities.getCompound(index);
			String id = entity.getString("id");
			entities.remove(index);
			LOGGER.info("已移除残留的实体：{}", id);
			changed = true;
		}

		return changed;
	}


	private static boolean cleanTileEntitiesList(ListTag tileEntities) {
		boolean changed = false;
		List<Integer> toRemove = new ArrayList<>();

		for (int i = 0; i < tileEntities.size(); i++) {
			CompoundTag tileEntity = tileEntities.getCompound(i);
			if (tileEntity.contains("id")) {
				String id = tileEntity.getString("id");
				int colonIndex = id.indexOf(':');
				if (colonIndex > 0) {
					String modId = id.substring(0, colonIndex);
					if (!Main.isModLoaded(modId)) {
						toRemove.add(i);
						LOGGER.info("发现引用不存在模组的方块实体: {}", id);
					}
				}
			}
		}


		for (int i = toRemove.size() - 1; i >= 0; i--) {
			int index = toRemove.get(i);
			CompoundTag tileEntity = tileEntities.getCompound(index);
			String id = tileEntity.getString("id");
			tileEntities.remove(index);
			LOGGER.info("已移除残留的方块实体：{}", id);
			changed = true;
		}

		return changed;
	}
}

package com.xinian.datapackloaderrorfix.processor.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.xinian.datapackloaderrorfix.processor.BaseProcessor;
import com.xinian.datapackloaderrorfix.util.BackupManager;
import com.xinian.datapackloaderrorfix.util.ModDetector;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class DatapackProcessor extends BaseProcessor {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String[] CONFIG_EXTENSIONS = {".json", ".mcmeta"};
    private static final String[] DATAPACK_DIRECTORIES = {"data"};
    private static final String[] REMOVABLE_KEYS = {
            "parent", "model", "texture", "item", "block", "entity",
            "type", "source", "target", "result", "ingredient",
            "advancement", "recipe", "loot_table", "structure"
    };

    public DatapackProcessor() {
        super(LogManager.getLogger());
    }

    public void processDatapacks(File worldDir) {
        logProcessStart("数据包", worldDir.getName());

        // 处理世界数据包目录
        File worldDatapacksDir = new File(worldDir, "datapacks");
        if (worldDatapacksDir.exists() && worldDatapacksDir.isDirectory()) {
            logger.info("正在处理世界数据包目录: {}", worldDatapacksDir.getPath());
            processDatapacksDirectory(worldDatapacksDir);
        }

        // 处理全局数据包目录
        File globalDatapacksDir = new File("datapacks");
        if (globalDatapacksDir.exists() && globalDatapacksDir.isDirectory()) {
            logger.info("正在处理全局数据包目录: {}", globalDatapacksDir.getPath());
            processDatapacksDirectory(globalDatapacksDir);
        }

        logProcessComplete("数据包", worldDir.getName(), true);
    }

    private void processDatapacksDirectory(File datapacksDir) {
        File[] datapacks = datapacksDir.listFiles(File::isDirectory);
        if (datapacks == null) {
            return;
        }

        for (File datapack : datapacks) {
            logger.info("正在处理数据包: {}", datapack.getName());
            processDatapack(datapack);
        }
    }

    private void processDatapack(File datapackDir) {

        File mcmetaFile = new File(datapackDir, "pack.mcmeta");
        if (mcmetaFile.exists()) {
            try {
                processConfigFile(mcmetaFile);
            } catch (Exception e) {
                logError("处理数据包元数据", mcmetaFile.getPath(), e);
            }
        }


        for (String dirName : DATAPACK_DIRECTORIES) {
            File dataDir = new File(datapackDir, dirName);
            if (dataDir.exists() && dataDir.isDirectory()) {
                try {
                    processDataDirectory(dataDir);
                } catch (Exception e) {
                    logError("处理数据目录", dataDir.getPath(), e);
                }
            }
        }
    }

    private void processDataDirectory(File dataDir) throws IOException {
        List<File> configFiles = findConfigFiles(dataDir);

        for (File configFile : configFiles) {
            try {
                processConfigFile(configFile);
            } catch (Exception e) {
                logError("处理配置文件", configFile.getPath(), e);
            }
        }
    }

    private List<File> findConfigFiles(File dataDir) throws IOException {
        try (Stream<Path> paths = Files.walk(dataDir.toPath())) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(this::isConfigFile)
                    .map(Path::toFile)
                    .toList();
        }
    }

    private boolean isConfigFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        for (String ext : CONFIG_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    private void processConfigFile(File configFile) throws IOException {
        logger.debug("正在处理配置文件: {}", configFile.getPath());

        if (!BackupManager.createBackup(configFile)) {
            logger.warn("无法创建备份，跳过配置文件: {}", configFile.getName());
            return;
        }

        JsonElement root;
        try (FileReader reader = new FileReader(configFile)) {
            root = JsonParser.parseReader(reader);
        }

        boolean changed = cleanJsonElement(root);

        if (changed) {
            logger.info("修复了配置文件: {}", configFile.getPath());
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(root, writer);
            }
        }
    }

    private boolean cleanJsonElement(JsonElement element) {
        if (element.isJsonObject()) {
            return cleanJsonObject(element.getAsJsonObject());
        } else if (element.isJsonArray()) {
            return cleanJsonArray(element.getAsJsonArray());
        } else if (element.isJsonPrimitive()) {
            return checkJsonPrimitive(element.getAsJsonPrimitive());
        }
        return false;
    }

    private boolean checkJsonPrimitive(JsonPrimitive primitive) {
        if (primitive.isString()) {
            String value = primitive.getAsString();
            if (ModDetector.isModReference(value)) {
                String modId = ModDetector.extractModId(value);
                if (modId != null && !ModDetector.isModLoaded(modId)) {
                    logger.warn("发现引用不存在模组的字符串: {}", value);

                }
            }
        }
        return false;
    }

    private boolean cleanJsonObject(JsonObject obj) {
        boolean changed = false;
        Set<String> keysToRemove = new HashSet<>();

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();


            if (ModDetector.isModReference(key)) {
                String modId = ModDetector.extractModId(key);
                if (modId != null && !ModDetector.isModLoaded(modId)) {
                    keysToRemove.add(key);
                    logger.info("发现引用不存在模组的键: {}", key);
                    changed = true;
                    continue;
                }
            }


            if (cleanJsonElement(value)) {
                changed = true;
            }


            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                String strValue = value.getAsString();
                if (ModDetector.isModReference(strValue)) {
                    String modId = ModDetector.extractModId(strValue);
                    if (modId != null && !ModDetector.isModLoaded(modId)) {
                        if (isRemovableKey(key)) {
                            keysToRemove.add(key);
                            logger.info("移除引用不存在模组的键值对: {} -> {}", key, strValue);
                            changed = true;
                        }
                    }
                }
            }
        }


        for (String key : keysToRemove) {
            obj.remove(key);
        }

        return changed;
    }

    private boolean cleanJsonArray(JsonArray array) {
        boolean changed = false;
        Iterator<JsonElement> iterator = array.iterator();

        while (iterator.hasNext()) {
            JsonElement element = iterator.next();


            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                String value = element.getAsString();
                if (ModDetector.isModReference(value)) {
                    String modId = ModDetector.extractModId(value);
                    if (modId != null && !ModDetector.isModLoaded(modId)) {
                        iterator.remove();
                        logger.info("从数组中移除引用不存在模组的元素: {}", value);
                        changed = true;
                        continue;
                    }
                }
            }


            if (element.isJsonObject() || element.isJsonArray()) {
                if (cleanJsonElement(element)) {
                    changed = true;
                }
            }
        }

        return changed;
    }

    private boolean isRemovableKey(String key) {
        for (String removableKey : REMOVABLE_KEYS) {
            if (key.equals(removableKey)) {
                return true;
            }
        }
        return false;
    }
}

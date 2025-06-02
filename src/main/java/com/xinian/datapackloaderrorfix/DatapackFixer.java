package com.xinian.datapackloaderrorfix;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

/**
 * 数据包配置修复工具
 * 扫描并修复数据包配置文件中引用不存在模组的内容
 */
public class DatapackFixer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    private static final String[] CONFIG_EXTENSIONS = {
            ".json", ".mcmeta"
    };


    private static final String[] DATAPACK_DIRECTORIES = {
            "data"
    };

    /**
     * 修复世界中的数据包配置
     */
    public static void fixDatapacks(File worldDir) {
        LOGGER.info("开始扫描并修复数据包配置...");


        File datapacksDir = new File(worldDir, "datapacks");
        if (datapacksDir.exists() && datapacksDir.isDirectory()) {
            LOGGER.info("正在处理世界数据包目录: {}", datapacksDir.getPath());
            processDatapacksDirectory(datapacksDir);
        }


        File globalDatapacksDir = new File("datapacks");
        if (globalDatapacksDir.exists() && globalDatapacksDir.isDirectory()) {
            LOGGER.info("正在处理全局数据包目录: {}", globalDatapacksDir.getPath());
            processDatapacksDirectory(globalDatapacksDir);
        }

        LOGGER.info("数据包配置修复完成");
    }


    private static void processDatapacksDirectory(File datapacksDir) {
        File[] datapacks = datapacksDir.listFiles(File::isDirectory);
        if (datapacks != null) {
            for (File datapack : datapacks) {
                LOGGER.info("正在处理数据包: {}", datapack.getName());
                processDatapack(datapack);
            }
        }
    }


    private static void processDatapack(File datapackDir) {

        File mcmetaFile = new File(datapackDir, "pack.mcmeta");
        if (mcmetaFile.exists()) {
            try {
                processConfigFile(mcmetaFile);
            } catch (Exception e) {
                LOGGER.error("处理数据包元数据时出错: {}", mcmetaFile.getPath(), e);
            }
        }


        for (String dirName : DATAPACK_DIRECTORIES) {
            File dataDir = new File(datapackDir, dirName);
            if (dataDir.exists() && dataDir.isDirectory()) {
                try {
                    processDataDirectory(dataDir);
                } catch (Exception e) {
                    LOGGER.error("处理数据目录时出错: {}", dataDir.getPath(), e);
                }
            }
        }
    }


    private static void processDataDirectory(File dataDir) throws IOException {

        List<File> configFiles = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(dataDir.toPath())) {
            configFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String fileName = path.getFileName().toString().toLowerCase();
                        for (String ext : CONFIG_EXTENSIONS) {
                            if (fileName.endsWith(ext)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .map(Path::toFile)
                    .toList();
        }


        for (File configFile : configFiles) {
            try {
                processConfigFile(configFile);
            } catch (Exception e) {
                LOGGER.error("处理配置文件时出错: {}", configFile.getPath(), e);
            }
        }
    }


    private static void processConfigFile(File configFile) throws IOException {
        LOGGER.debug("正在处理配置文件: {}", configFile.getPath());


        if (Main.createBackups) {
            File backup = new File(configFile.getParentFile(), configFile.getName() + ".bak");
            if (!backup.exists()) {
                Files.copy(configFile.toPath(), backup.toPath());
            }
        }


        JsonElement root;
        try (FileReader reader = new FileReader(configFile)) {
            root = JsonParser.parseReader(reader);
        }


        boolean changed = cleanJsonElement(root);


        if (changed) {
            LOGGER.info("修复了配置文件: {}", configFile.getPath());
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(root, writer);
            }
        }
    }


    private static boolean cleanJsonElement(JsonElement element) {
        boolean changed = false;

        if (element.isJsonObject()) {
            changed = cleanJsonObject(element.getAsJsonObject());
        } else if (element.isJsonArray()) {
            changed = cleanJsonArray(element.getAsJsonArray());
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                String value = primitive.getAsString();
                if (isModReference(value)) {
                    String modId = extractModId(value);
                    if (!Main.isModLoaded(modId)) {
                        // 无法直接修改JsonPrimitive的值，但我们可以记录这个问题
                        LOGGER.warn("发现引用不存在模组的字符串: {}", value);

                    }
                }
            }
        }

        return changed;
    }

    /**
     * 清理JsonObject中引用不存在模组的内容
     */
    private static boolean cleanJsonObject(JsonObject obj) {
        boolean changed = false;


        Set<String> keysToRemove = new HashSet<>();


        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();


            if (isModReference(key)) {
                String modId = extractModId(key);
                if (!Main.isModLoaded(modId)) {
                    keysToRemove.add(key);
                    LOGGER.info("发现引用不存在模组的键: {}", key);
                    changed = true;
                    continue;
                }
            }


            if (cleanJsonElement(value)) {
                changed = true;
            }


            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                String strValue = value.getAsString();
                if (isModReference(strValue)) {
                    String modId = extractModId(strValue);
                    if (!Main.isModLoaded(modId)) {

                        if (isRemovableKey(key)) {
                            keysToRemove.add(key);
                            LOGGER.info("移除引用不存在模组的键值对: {} -> {}", key, strValue);
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

    /**
     * 清理JsonArray中引用不存在模组的内容
     */
    private static boolean cleanJsonArray(JsonArray array) {
        boolean changed = false;


        Iterator<JsonElement> iterator = array.iterator();
        while (iterator.hasNext()) {
            JsonElement element = iterator.next();


            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                String value = element.getAsString();
                if (isModReference(value)) {
                    String modId = extractModId(value);
                    if (!Main.isModLoaded(modId)) {
                        iterator.remove();
                        LOGGER.info("从数组中移除引用不存在模组的元素: {}", value);
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


    private static boolean isModReference(String str) {

        return str.contains(":");
    }


    private static String extractModId(String reference) {
        int colonIndex = reference.indexOf(':');
        if (colonIndex > 0) {
            return reference.substring(0, colonIndex);
        }
        return reference;
    }

    /**
     * 判断键是否可以安全移除（当值引用不存在的模组时）
     */
    private static boolean isRemovableKey(String key) {

        String[] removableKeys = {
                "parent", "model", "texture", "item", "block", "entity",
                "type", "source", "target", "result", "ingredient",
                "advancement", "recipe", "loot_table", "structure"
        };

        for (String removableKey : removableKeys) {
            if (key.equals(removableKey)) {
                return true;
            }
        }

        return false;
    }
}


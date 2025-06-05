package com.xinian.datapackloaderrorfix.constant;

public class Constants {


    public static final class FileExtensions {
        public static final String JSON = ".json";
        public static final String MCMETA = ".mcmeta";
        public static final String MCA = ".mca";
        public static final String DAT = ".dat";
        public static final String BACKUP = ".bak";
    }


    public static final class Directories {
        public static final String DATAPACKS = "datapacks";
        public static final String DATA = "data";
        public static final String REGION = "region";
        public static final String DIMENSIONS = "dimensions";
        public static final String DIM_NETHER = "DIM-1";
        public static final String DIM_END = "DIM1";
    }


    public static final class FileNames {
        public static final String LEVEL_DAT = "level.dat";
        public static final String PACK_MCMETA = "pack.mcmeta";
    }


    public static final class NbtTags {
        public static final String DATA = "Data";
        public static final String PLAYER = "Player";
        public static final String INVENTORY = "Inventory";
        public static final String ENDER_ITEMS = "EnderItems";
        public static final String WORLD_GEN_SETTINGS = "WorldGenSettings";
        public static final String DIMENSIONS = "dimensions";
        public static final String ID = "id";
        public static final String ENTITIES = "Entities";
        public static final String TILE_ENTITIES = "TileEntities";
    }


    public static final class JsonKeys {
        public static final String[] REMOVABLE_KEYS = {
                "parent", "model", "texture", "item", "block", "entity",
                "type", "source", "target", "result", "ingredient",
                "advancement", "recipe", "loot_table", "structure"
        };
    }


    public static final class LogMessages {
        public static final String PROCESSING_START = "开始处理: {}";
        public static final String PROCESSING_COMPLETE = "处理完成: {}";
        public static final String PROCESSING_ERROR = "处理时出错: {}";
        public static final String BACKUP_CREATED = "已创建备份: {}";
        public static final String BACKUP_FAILED = "创建备份失败: {}";
        public static final String MOD_REFERENCE_FOUND = "发现模组引用: {}";
        public static final String MOD_REFERENCE_REMOVED = "已移除模组引用: {}";
    }
}

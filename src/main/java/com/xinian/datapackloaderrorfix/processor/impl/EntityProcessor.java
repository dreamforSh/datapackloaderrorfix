package com.xinian.datapackloaderrorfix.processor.impl;

import com.xinian.datapackloaderrorfix.processor.BaseProcessor;
import com.xinian.datapackloaderrorfix.util.ModDetector;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;

public class EntityProcessor extends BaseProcessor {

    public EntityProcessor() {
        super(LogManager.getLogger());
    }

    public boolean cleanEntitiesList(ListTag entities, String context) {
        boolean changed = false;
        List<Integer> toRemove = new ArrayList<>();

        for (int i = 0; i < entities.size(); i++) {
            CompoundTag entity = entities.getCompound(i);
            if (entity.contains("id")) {
                String id = entity.getString("id");
                String modId = ModDetector.extractModId(id);

                if (modId != null && !ModDetector.isModLoaded(modId)) {
                    toRemove.add(i);
                    logger.info("发现{}中引用不存在模组的实体: {}", context, id);
                }
            }
        }


        for (int i = toRemove.size() - 1; i >= 0; i--) {
            int index = toRemove.get(i);
            CompoundTag entity = entities.getCompound(index);
            String id = entity.getString("id");
            entities.remove(index);
            logger.info("已从{}移除残留实体: {}", context, id);
            changed = true;
        }

        return changed;
    }

    public boolean cleanTileEntitiesList(ListTag tileEntities, String context) {
        boolean changed = false;
        List<Integer> toRemove = new ArrayList<>();

        for (int i = 0; i < tileEntities.size(); i++) {
            CompoundTag tileEntity = tileEntities.getCompound(i);
            if (tileEntity.contains("id")) {
                String id = tileEntity.getString("id");
                String modId = ModDetector.extractModId(id);

                if (modId != null && !ModDetector.isModLoaded(modId)) {
                    toRemove.add(i);
                    logger.info("发现{}中引用不存在模组的方块实体: {}", context, id);
                }
            }
        }


        for (int i = toRemove.size() - 1; i >= 0; i--) {
            int index = toRemove.get(i);
            CompoundTag tileEntity = tileEntities.getCompound(index);
            String id = tileEntity.getString("id");
            tileEntities.remove(index);
            logger.info("已从{}移除残留方块实体: {}", context, id);
            changed = true;
        }

        return changed;
    }
}




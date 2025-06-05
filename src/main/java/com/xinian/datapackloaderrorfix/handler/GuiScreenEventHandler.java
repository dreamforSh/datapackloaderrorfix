package com.xinian.datapackloaderrorfix.handler;

import com.xinian.datapackloaderrorfix.Main;
import com.xinian.datapackloaderrorfix.service.CleanupService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = Main.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class GuiScreenEventHandler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Component BUTTON_TEXT = Component.translatable("gui.datapackloaderrorfix.BUTTON_TEXT");
    private static final Component SUCCESS_TEXT = Component.translatable("gui.datapackloaderrorfix.SUCCESS_TEXT");
    private static final Component FAILURE_TEXT = Component.translatable("gui.datapackloaderrorfix.FAILURE_TEXT");

    private static Field levelAccessField = null;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();

        if (screen instanceof EditWorldScreen editWorldScreen) {

            Button cleanButton = Button.builder(BUTTON_TEXT, (button) -> {
                button.active = false;
                handleCleanButtonClick(editWorldScreen, button);
            }).bounds(screen.width / 2 - 100, screen.height - 56, 200, 20).build();


            event.addListener(cleanButton);
        }
    }

    private static void handleCleanButtonClick(EditWorldScreen screen, Button button) {
        LevelStorageSource.LevelStorageAccess levelAccess = getLevelAccess(screen);

        if (levelAccess == null) {
            LOGGER.error("无法获取世界存档信息(levelAccess)，清理操作中止。");
            button.active = true;
            return;
        }

        String levelId = levelAccess.getLevelId();
        LOGGER.info("开始手动清理世界数据: {}", levelId);

        new CleanupService().cleanupWorldAsync(levelAccess.getWorldDir().toFile())
                .thenRun(() -> {
                    LOGGER.info("世界数据清理成功: {}", levelId);
                    Minecraft.getInstance().execute(() -> {
                        screen.getMinecraft().getToasts().addToast(new net.minecraft.client.gui.components.toasts.SystemToast(
                                net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds.WORLD_BACKUP,
                                SUCCESS_TEXT,
                                null));
                        button.active = true;
                    });
                })
                .exceptionally(ex -> {
                    LOGGER.error("世界数据清理失败: {}", levelId, ex);
                    Minecraft.getInstance().execute(() -> {
                        screen.getMinecraft().getToasts().addToast(new net.minecraft.client.gui.components.toasts.SystemToast(
                                net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds.WORLD_BACKUP,
                                FAILURE_TEXT,
                                Component.literal(ex.getMessage())));
                        button.active = true;
                    });
                    return null;
                });
    }

    private static LevelStorageSource.LevelStorageAccess getLevelAccess(EditWorldScreen screen) {
        try {
            if (levelAccessField == null) {
                Field field = EditWorldScreen.class.getDeclaredField("levelAccess");
                field.setAccessible(true);
                levelAccessField = field;
            }
            return (LevelStorageSource.LevelStorageAccess) levelAccessField.get(screen);
        } catch (Exception e) {
            LOGGER.error("反射失败：无法找到或访问 'levelAccess' 字段。", e);
            return null;
        }
    }
}

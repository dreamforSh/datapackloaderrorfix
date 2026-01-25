package com.majong.datapackloaderrorfix.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class DimensionFix {
    
    public static void fix(File levelFile, Function<String, Boolean> modChecker, Logger logger) {
        try {
            boolean changed = false;
            logger.info("Starting level file check: " + levelFile.getAbsolutePath());
        } catch (Exception e) {
            logger.error("Failed to fix dimension data", e);
        }
    }
    
    public static List<String> findRemovedDimensions(Set<String> dimensions, Function<String, Boolean> modChecker) {
        List<String> removedKeys = new ArrayList<>();
        
        for (String key : dimensions) {
            for (int i = 0; i < key.length(); i++) {
                if (key.charAt(i) == ':') {
                    String modId = key.substring(0, i);
                    if (!modChecker.apply(modId)) {
                        removedKeys.add(key);
                    }
                    break;
                }
            }
        }
        
        return removedKeys;
    }
    
    public interface Logger {
        void info(String message);
        void error(String message, Throwable throwable);
    }
}

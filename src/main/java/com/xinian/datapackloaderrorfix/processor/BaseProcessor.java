package com.xinian.datapackloaderrorfix.processor;

import org.apache.logging.log4j.Logger;

public abstract class BaseProcessor {
    protected final Logger logger;

    protected BaseProcessor(Logger logger) {
        this.logger = logger;
    }

    protected void logProcessStart(String processorName, String target) {
        logger.info("开始{}处理: {}", processorName, target);
    }

    protected void logProcessComplete(String processorName, String target, boolean hasChanges) {
        if (hasChanges) {
            logger.info("{}处理完成，已应用修改: {}", processorName, target);
        } else {
            logger.info("{}处理完成，未发现需要清理的数据: {}", processorName, target);
        }
    }

    protected void logError(String operation, String target, Exception e) {
        logger.error("{}时出错: {}", operation, target, e);
    }
}
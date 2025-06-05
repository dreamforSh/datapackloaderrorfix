package com.xinian.datapackloaderrorfix.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FileUtils {
    private static final Logger LOGGER = LogManager.getLogger();

    public static List<File> findFiles(File directory, Predicate<Path> filter) throws IOException {
        if (!directory.exists() || !directory.isDirectory()) {
            return List.of();
        }

        try (Stream<Path> paths = Files.walk(directory.toPath())) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(filter)
                    .map(Path::toFile)
                    .toList();
        }
    }

    public static List<File> findDirectories(File directory, Predicate<Path> filter) throws IOException {
        if (!directory.exists() || !directory.isDirectory()) {
            return List.of();
        }

        try (Stream<Path> paths = Files.walk(directory.toPath())) {
            return paths
                    .filter(path -> path.toFile().isDirectory())
                    .filter(filter)
                    .map(Path::toFile)
                    .toList();
        }
    }

    public static boolean hasExtension(Path path, String... extensions) {
        String fileName = path.getFileName().toString().toLowerCase();
        for (String ext : extensions) {
            if (fileName.endsWith(ext.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidFile(File file) {
        return file != null && file.exists() && file.isFile() && file.canRead();
    }

    public static boolean isValidDirectory(File directory) {
        return directory != null && directory.exists() && directory.isDirectory() && directory.canRead();
    }

    public static long getFileSize(File file) {
        try {
            return Files.size(file.toPath());
        } catch (IOException e) {
            LOGGER.warn("无法获取文件大小: {}", file.getName(), e);
            return 0L;
        }
    }
}

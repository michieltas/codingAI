package nl.mihaly.main;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Stores and loads cached UI values such as:
 * - className
 * - specification
 * - packageName
 *
 * This allows the user to continue where they left off.
 */
public class CacheManager {

    private final Consumer<String> logger;
    private final Path cacheFile;

    public CacheManager(Consumer<String> logger) {
        this.logger = logger;
        this.cacheFile = Paths.get(System.getProperty("user.home"), ".codingai.cache");
    }

    /**
     * Loads cached values from disk.
     */
    public CachedValues load() {
        Properties props = new Properties();

        if (Files.exists(cacheFile)) {
            try {
                props.load(Files.newBufferedReader(cacheFile, StandardCharsets.UTF_8));
                logger.accept("Loaded cache from " + cacheFile);
            } catch (IOException e) {
                logger.accept("Failed to load cache: " + e.getMessage());
            }
        }

        String className = props.getProperty("className", "");
        String specification = props.getProperty("specification", "");
        String packageName = props.getProperty("packageName", "");

        return new CachedValues(className, specification, packageName);
    }

    /**
     * Saves cached values to disk.
     */
    public void save(String className, String specification, String packageName) {
        Properties props = new Properties();
        props.setProperty("className", className == null ? "" : className);
        props.setProperty("specification", specification == null ? "" : specification);
        props.setProperty("packageName", packageName == null ? "" : packageName);

        try (var writer = Files.newBufferedWriter(cacheFile, StandardCharsets.UTF_8)) {
            props.store(writer, "CodingAI cache");
            logger.accept("Saved cache to " + cacheFile);
        } catch (IOException e) {
            logger.accept("Failed to save cache: " + e.getMessage());
        }
    }

    /**
     * Simple holder for cached values.
     */
    public static class CachedValues {
        public final String className;
        public final String specification;
        public final String packageName;

        public CachedValues(String className, String specification, String packageName) {
            this.className = className;
            this.specification = specification;
            this.packageName = packageName;
        }
    }

    public void clear() {
        try {
            if (Files.exists(cacheFile)) {
                Files.delete(cacheFile);
                logger.accept("Cache file removed: " + cacheFile);
            } else {
                logger.accept("No cache file to remove.");
            }
        } catch (IOException e) {
            logger.accept("Failed to remove cache file: " + e.getMessage());
        }
    }
}

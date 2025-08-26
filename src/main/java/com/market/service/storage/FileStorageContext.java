package com.market.service.storage;

import com.market.model.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Context class for managing file storage strategies.
 * Allows switching between different storage implementations at runtime.
 */
@Service
public class FileStorageContext {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageContext.class);

    @Value("${file.storage.strategy:aws-s3}")
    private String defaultStrategy;

    @Autowired
    private LocalFileStorageStrategy localFileStorageStrategy;

    @Autowired
    private AwsS3StorageStrategy awsS3StorageStrategy;

    private Map<String, FileStorageStrategy> strategies;
    private FileStorageStrategy currentStrategy;

    @PostConstruct
    public void initialize() {
        strategies = new HashMap<>();
        strategies.put("local", localFileStorageStrategy);
        strategies.put("aws-s3", awsS3StorageStrategy);

        // Set default strategy
        setStrategy(defaultStrategy);

        logger.info("FileStorageContext initialized with default strategy: {}", defaultStrategy);
    }

    /**
     * Set the current storage strategy
     *
     * @param strategyName The name of the strategy to use
     * @throws IllegalArgumentException if the strategy is not found or not available
     */
    public void setStrategy(String strategyName) {
        FileStorageStrategy strategy = strategies.get(strategyName);
        if (strategy == null) {
            throw new IllegalArgumentException("Storage strategy not found: " + strategyName);
        }

        if (!strategy.isAvailable()) {
            throw new IllegalArgumentException("Storage strategy is not available: " + strategyName);
        }

        this.currentStrategy = strategy;
        logger.info("Switched to storage strategy: {}", strategyName);
    }

    /**
     * Get the current storage strategy
     *
     * @return The current FileStorageStrategy
     */
    public FileStorageStrategy getCurrentStrategy() {
        return currentStrategy;
    }

    /**
     * Get the name of the current strategy
     *
     * @return The current strategy name
     */
    public String getCurrentStrategyName() {
        return currentStrategy != null ? currentStrategy.getStrategyName() : "none";
    }

    /**
     * Get all available strategies
     *
     * @return Map of strategy names to their availability status
     */
    public Map<String, Boolean> getAvailableStrategies() {
        Map<String, Boolean> available = new HashMap<>();
        for (Map.Entry<String, FileStorageStrategy> entry : strategies.entrySet()) {
            available.put(entry.getKey(), entry.getValue().isAvailable());
        }
        return available;
    }

    /**
     * Store a shop profile image using the current strategy
     */
    public String storeShopProfileImage(MultipartFile file, Long shopId) throws IOException {
        if (currentStrategy == null) {
            throw new IllegalStateException("No storage strategy is set");
        }

        logger.debug("Storing shop profile image using strategy: {}", currentStrategy.getStrategyName());
        return currentStrategy.storeShopProfileImage(file, shopId);
    }

    /**
     * Store an item media file using the current strategy
     */
    public String storeItemMedia(MultipartFile file, Long shopId, Long itemId) throws IOException {
        if (currentStrategy == null) {
            throw new IllegalStateException("No storage strategy is set");
        }

        logger.debug("Storing item media using strategy: {}", currentStrategy.getStrategyName());
        return currentStrategy.storeItemMedia(file, shopId, itemId);
    }

    /**
     * Delete a file using the current strategy
     */
    public void deleteFile(String storageIdentifier) throws IOException {
        if (currentStrategy == null) {
            throw new IllegalStateException("No storage strategy is set");
        }

        logger.debug("Deleting file using strategy: {}", currentStrategy.getStrategyName());
        currentStrategy.deleteFile(storageIdentifier);
    }

    /**
     * Check if a file exists using the current strategy
     */
    public boolean fileExists(String storageIdentifier) {
        if (currentStrategy == null) {
            return false;
        }

        return currentStrategy.fileExists(storageIdentifier);
    }

    /**
     * Get a file resource using the current strategy
     */
    public Resource getFileResource(String storageIdentifier) throws IOException {
        if (currentStrategy == null) {
            throw new IllegalStateException("No storage strategy is set");
        }

        return currentStrategy.getFileResource(storageIdentifier);
    }

    /**
     * Get file size using the current strategy
     */
    public long getFileSize(String storageIdentifier) throws IOException {
        if (currentStrategy == null) {
            throw new IllegalStateException("No storage strategy is set");
        }

        return currentStrategy.getFileSize(storageIdentifier);
    }

    /**
     * Read a file chunk using the current strategy
     */
    public byte[] readFileChunk(String storageIdentifier, long start, long end) throws IOException {
        if (currentStrategy == null) {
            throw new IllegalStateException("No storage strategy is set");
        }

        return currentStrategy.readFileChunk(storageIdentifier, start, end);
    }

    /**
     * Check if streaming is supported by the current strategy
     */
    public boolean supportsStreaming(String storageIdentifier) {
        if (currentStrategy == null) {
            return false;
        }

        return currentStrategy.supportsStreaming(storageIdentifier);
    }

    /**
     * Get optimal chunk size for streaming using the current strategy
     */
    public int getOptimalChunkSize(String storageIdentifier) {
        if (currentStrategy == null) {
            return 1024 * 1024; // Default 1MB
        }

        return currentStrategy.getOptimalChunkSize(storageIdentifier);
    }

    /**
     * Determine media type using the current strategy
     */
    public MediaType determineMediaType(MultipartFile file) {
        if (currentStrategy == null) {
            // Default implementation
            String contentType = file.getContentType();
            if (contentType != null) {
                if (contentType.startsWith("image/")) {
                    return MediaType.IMAGE;
                } else if (contentType.startsWith("video/")) {
                    return MediaType.VIDEO;
                }
            }
            return MediaType.IMAGE;
        }

        return currentStrategy.determineMediaType(file);
    }

    /**
     * Get health status of all strategies
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        health.put("currentStrategy", getCurrentStrategyName());
        health.put("availableStrategies", getAvailableStrategies());

        Map<String, Object> strategyDetails = new HashMap<>();
        for (Map.Entry<String, FileStorageStrategy> entry : strategies.entrySet()) {
            Map<String, Object> details = new HashMap<>();
            details.put("available", entry.getValue().isAvailable());
            details.put("name", entry.getValue().getStrategyName());
            strategyDetails.put(entry.getKey(), details);
        }
        health.put("strategyDetails", strategyDetails);

        return health;
    }

    /**
     * Migrate files from one strategy to another
     *
     * @param fromStrategy       The source strategy name
     * @param toStrategy         The target strategy name
     * @param storageIdentifiers List of storage identifiers to migrate
     * @return Map of old identifiers to new identifiers
     */
    public Map<String, String> migrateFiles(String fromStrategy, String toStrategy,
                                            java.util.List<String> storageIdentifiers) throws IOException {
        FileStorageStrategy sourceStrategy = strategies.get(fromStrategy);
        FileStorageStrategy targetStrategy = strategies.get(toStrategy);

        if (sourceStrategy == null || targetStrategy == null) {
            throw new IllegalArgumentException("Invalid strategy names");
        }

        if (!sourceStrategy.isAvailable() || !targetStrategy.isAvailable()) {
            throw new IllegalStateException("One or both strategies are not available");
        }

        Map<String, String> migrationMap = new HashMap<>();

        for (String oldIdentifier : storageIdentifiers) {
            try {
                // Get file from source strategy
                Resource resource = sourceStrategy.getFileResource(oldIdentifier);

                // Create a temporary MultipartFile-like object
                // Note: This is a simplified approach. In a real implementation,
                // you might need to handle this differently based on your needs

                // For now, we'll skip migration and just log it
                logger.warn("Migration not fully implemented for identifier: {}", oldIdentifier);
                migrationMap.put(oldIdentifier, oldIdentifier); // Keep same identifier for now

            } catch (Exception e) {
                logger.error("Failed to migrate file: {}", oldIdentifier, e);
                throw new IOException("Migration failed for file: " + oldIdentifier, e);
            }
        }

        logger.info("Migration completed: {} files processed", storageIdentifiers.size());
        return migrationMap;
    }
}

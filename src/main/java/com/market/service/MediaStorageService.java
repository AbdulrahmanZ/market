package com.market.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.model.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing media metadata and storage organization.
 * This service maintains JSON files that track media information for shops and items.
 * It works alongside FileStorageService which handles the actual file operations.
 */
@Service
public class MediaStorageService {

    private static final Logger logger = LoggerFactory.getLogger(MediaStorageService.class);

    @Value("${media.storage.base-path:media-storage}")
    private String baseStoragePath;

    private final ObjectMapper objectMapper;

    public MediaStorageService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules(); // For LocalDateTime support
    }

    // ==================== ITEM MEDIA MANAGEMENT ====================

    /**
     * Save or update media URL for an item in the shop's media file
     */
    public void saveItemMedia(Long shopId, Long itemId, String mediaUrl, MediaType mediaType, String fileName) {
        logger.debug("Saving item media: shopId={}, itemId={}, mediaUrl={}, type={}",
                shopId, itemId, mediaUrl, mediaType);

        try {
            Path shopDir = createShopDirectory(shopId);
            Path mediaFile = shopDir.resolve("items-media.json");

            Map<String, Object> mediaData = loadMediaData(mediaFile);

            Map<String, Object> itemMedia = new HashMap<>();
            itemMedia.put("itemId", itemId);
            itemMedia.put("mediaUrl", mediaUrl);
            itemMedia.put("mediaType", mediaType != null ? mediaType.toString() : "UNKNOWN");
            itemMedia.put("fileName", fileName);
            itemMedia.put("lastUpdated", LocalDateTime.now().toString());
            itemMedia.put("shopId", shopId);

            mediaData.put("item_" + itemId, itemMedia);

            saveMediaData(mediaFile, mediaData);
            logger.info("Successfully saved media metadata for item {} in shop {}", itemId, shopId);

        } catch (IOException e) {
            logger.error("Failed to save media data for item {} in shop {}", itemId, shopId, e);
            throw new RuntimeException("Failed to save media data", e);
        }
    }

    /**
     * Get media URL for a specific item
     */
    public Optional<Map<String, Object>> getItemMedia(Long shopId, Long itemId) {
        logger.debug("Getting item media: shopId={}, itemId={}", shopId, itemId);

        try {
            Path shopDir = Paths.get(baseStoragePath, "shop-" + shopId);
            Path mediaFile = shopDir.resolve("items-media.json");

            if (!Files.exists(mediaFile)) {
                logger.debug("Media file not found for shop {}", shopId);
                return Optional.empty();
            }

            Map<String, Object> mediaData = loadMediaData(mediaFile);
            Object itemMedia = mediaData.get("item_" + itemId);

            if (itemMedia instanceof Map) {
                logger.debug("Found media for item {} in shop {}", itemId, shopId);
                return Optional.of((Map<String, Object>) itemMedia);
            }

            logger.debug("No media found for item {} in shop {}", itemId, shopId);
            return Optional.empty();

        } catch (IOException e) {
            logger.error("Error getting item media: shopId={}, itemId={}", shopId, itemId, e);
            return Optional.empty();
        }
    }

    /**
     * Get all media URLs for a shop
     */
    public Map<String, Object> getAllShopMedia(Long shopId) {
        logger.debug("Getting all shop media: shopId={}", shopId);

        try {
            Path shopDir = Paths.get(baseStoragePath, "shop-" + shopId);
            Path mediaFile = shopDir.resolve("items-media.json");

            if (!Files.exists(mediaFile)) {
                logger.debug("No media file found for shop {}", shopId);
                return new HashMap<>();
            }

            Map<String, Object> mediaData = loadMediaData(mediaFile);
            logger.debug("Retrieved {} media entries for shop {}", mediaData.size(), shopId);
            return mediaData;

        } catch (IOException e) {
            logger.error("Error getting all shop media: shopId={}", shopId, e);
            return new HashMap<>();
        }
    }

    /**
     * Delete media for a specific item
     */
    public void deleteItemMedia(Long shopId, Long itemId) {
        logger.debug("Deleting item media: shopId={}, itemId={}", shopId, itemId);

        try {
            Path shopDir = Paths.get(baseStoragePath, "shop-" + shopId);
            Path mediaFile = shopDir.resolve("items-media.json");

            if (!Files.exists(mediaFile)) {
                logger.debug("No media file to delete from for shop {}", shopId);
                return;
            }

            Map<String, Object> mediaData = loadMediaData(mediaFile);
            Object removed = mediaData.remove("item_" + itemId);

            if (removed != null) {
                saveMediaData(mediaFile, mediaData);
                logger.info("Successfully deleted media metadata for item {} in shop {}", itemId, shopId);
            } else {
                logger.debug("No media metadata found to delete for item {} in shop {}", itemId, shopId);
            }

        } catch (IOException e) {
            logger.error("Failed to delete media data for item {} in shop {}", itemId, shopId, e);
            throw new RuntimeException("Failed to delete media data", e);
        }
    }

    /**
     * Delete all media for a shop
     */
    public void deleteShopMedia(Long shopId) {
        logger.debug("Deleting all shop media: shopId={}", shopId);

        try {
            Path shopDir = Paths.get(baseStoragePath, "shop-" + shopId);
            Path mediaFile = shopDir.resolve("items-media.json");

            if (Files.exists(mediaFile)) {
                Files.delete(mediaFile);
                logger.info("Successfully deleted all media metadata for shop {}", shopId);
            } else {
                logger.debug("No media file found to delete for shop {}", shopId);
            }

            // Also try to delete the shop directory if it's empty
            if (Files.exists(shopDir) && isDirectoryEmpty(shopDir)) {
                Files.delete(shopDir);
                logger.debug("Deleted empty shop directory for shop {}", shopId);
            }

        } catch (IOException e) {
            logger.error("Failed to delete shop media for shop {}", shopId, e);
            throw new RuntimeException("Failed to delete shop media", e);
        }
    }

    // ==================== MEDIA STATISTICS ====================

    /**
     * Get media statistics for a shop
     */
    public Map<String, Object> getShopMediaStats(Long shopId) {
        logger.debug("Getting media stats for shop: {}", shopId);

        Map<String, Object> stats = new HashMap<>();

        try {
            Map<String, Object> allMedia = getAllShopMedia(shopId);

            int totalItems = allMedia.size();
            int imageCount = 0;
            int videoCount = 0;

            for (Object mediaObj : allMedia.values()) {
                if (mediaObj instanceof Map) {
                    Map<String, Object> media = (Map<String, Object>) mediaObj;
                    String mediaType = (String) media.get("mediaType");

                    if ("IMAGE".equals(mediaType)) {
                        imageCount++;
                    } else if ("VIDEO".equals(mediaType)) {
                        videoCount++;
                    }
                }
            }

            stats.put("shopId", shopId);
            stats.put("totalItems", totalItems);
            stats.put("imageCount", imageCount);
            stats.put("videoCount", videoCount);
            stats.put("lastChecked", LocalDateTime.now().toString());

            logger.debug("Media stats for shop {}: {} total, {} images, {} videos",
                    shopId, totalItems, imageCount, videoCount);

        } catch (Exception e) {
            logger.error("Error calculating media stats for shop {}", shopId, e);
            stats.put("error", "Failed to calculate stats: " + e.getMessage());
        }

        return stats;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Create shop directory for media storage
     */
    private Path createShopDirectory(Long shopId) throws IOException {
        Path shopDir = Paths.get(baseStoragePath, "shop-" + shopId);
        Files.createDirectories(shopDir);
        logger.debug("Created/verified shop directory: {}", shopDir);
        return shopDir;
    }

    /**
     * Load media data from JSON file
     */
    private Map<String, Object> loadMediaData(Path mediaFile) throws IOException {
        if (!Files.exists(mediaFile)) {
            logger.debug("Media file does not exist, returning empty map: {}", mediaFile);
            return new HashMap<>();
        }

        String content = Files.readString(mediaFile);
        if (content.trim().isEmpty()) {
            logger.debug("Media file is empty, returning empty map: {}", mediaFile);
            return new HashMap<>();
        }

        try {
            Map<String, Object> data = objectMapper.readValue(content, Map.class);
            logger.debug("Loaded {} entries from media file: {}", data.size(), mediaFile);
            return data;
        } catch (Exception e) {
            logger.warn("Failed to parse media file, returning empty map: {}", mediaFile, e);
            return new HashMap<>();
        }
    }

    /**
     * Save media data to JSON file
     */
    private void saveMediaData(Path mediaFile, Map<String, Object> mediaData) throws IOException {
        String jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(mediaData);
        Files.writeString(mediaFile, jsonContent);
        logger.debug("Saved {} entries to media file: {}", mediaData.size(), mediaFile);
    }

    /**
     * Check if directory is empty
     */
    private boolean isDirectoryEmpty(Path directory) throws IOException {
        try (var stream = Files.list(directory)) {
            return stream.findAny().isEmpty();
        }
    }

    // ==================== HEALTH CHECK ====================

    /**
     * Check service health and storage accessibility
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();

        try {
            Path basePath = Paths.get(baseStoragePath);

            health.put("service", "MediaStorageService");
            health.put("status", "UP");
            health.put("baseStoragePath", baseStoragePath);
            health.put("basePathExists", Files.exists(basePath));
            health.put("basePathWritable", Files.isWritable(basePath.getParent()));
            health.put("timestamp", LocalDateTime.now().toString());

            // Try to create base directory if it doesn't exist
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
                health.put("basePathCreated", true);
            }

        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            logger.error("Health check failed for MediaStorageService", e);
        }

        return health;
    }
}

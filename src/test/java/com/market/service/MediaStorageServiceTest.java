package com.market.service;

import com.market.model.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MediaStorageServiceTest {

    private MediaStorageService mediaStorageService;
    
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mediaStorageService = new MediaStorageService();
        // Set the base storage path to our temp directory
        ReflectionTestUtils.setField(mediaStorageService, "baseStoragePath", tempDir.toString());
    }

    @Test
    void testSaveAndGetItemMedia() {
        // Given
        Long shopId = 1L;
        Long itemId = 100L;
        String mediaUrl = "items/shop-1/item-100-uuid.jpg";
        MediaType mediaType = MediaType.IMAGE;
        String fileName = "test-image.jpg";

        // When
        mediaStorageService.saveItemMedia(shopId, itemId, mediaUrl, mediaType, fileName);

        // Then
        Optional<Map<String, Object>> result = mediaStorageService.getItemMedia(shopId, itemId);
        assertTrue(result.isPresent());
        
        Map<String, Object> mediaData = result.get();
        assertEquals(itemId, mediaData.get("itemId"));
        assertEquals(mediaUrl, mediaData.get("mediaUrl"));
        assertEquals("IMAGE", mediaData.get("mediaType"));
        assertEquals(fileName, mediaData.get("fileName"));
        assertEquals(shopId, mediaData.get("shopId"));
        assertNotNull(mediaData.get("lastUpdated"));
    }

    @Test
    void testGetNonExistentItemMedia() {
        // Given
        Long shopId = 1L;
        Long itemId = 999L;

        // When
        Optional<Map<String, Object>> result = mediaStorageService.getItemMedia(shopId, itemId);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testGetAllShopMedia() {
        // Given
        Long shopId = 1L;
        
        // Save multiple items
        mediaStorageService.saveItemMedia(shopId, 1L, "items/shop-1/item-1.jpg", MediaType.IMAGE, "item1.jpg");
        mediaStorageService.saveItemMedia(shopId, 2L, "items/shop-1/item-2.mp4", MediaType.VIDEO, "item2.mp4");
        mediaStorageService.saveItemMedia(shopId, 3L, "items/shop-1/item-3.png", MediaType.IMAGE, "item3.png");

        // When
        Map<String, Object> allMedia = mediaStorageService.getAllShopMedia(shopId);

        // Then
        assertEquals(3, allMedia.size());
        assertTrue(allMedia.containsKey("item_1"));
        assertTrue(allMedia.containsKey("item_2"));
        assertTrue(allMedia.containsKey("item_3"));
    }

    @Test
    void testDeleteItemMedia() {
        // Given
        Long shopId = 1L;
        Long itemId = 100L;
        mediaStorageService.saveItemMedia(shopId, itemId, "test-url", MediaType.IMAGE, "test.jpg");
        
        // Verify it exists
        assertTrue(mediaStorageService.getItemMedia(shopId, itemId).isPresent());

        // When
        mediaStorageService.deleteItemMedia(shopId, itemId);

        // Then
        assertFalse(mediaStorageService.getItemMedia(shopId, itemId).isPresent());
    }

    @Test
    void testDeleteShopMedia() {
        // Given
        Long shopId = 1L;
        mediaStorageService.saveItemMedia(shopId, 1L, "test-url-1", MediaType.IMAGE, "test1.jpg");
        mediaStorageService.saveItemMedia(shopId, 2L, "test-url-2", MediaType.VIDEO, "test2.mp4");
        
        // Verify they exist
        assertEquals(2, mediaStorageService.getAllShopMedia(shopId).size());

        // When
        mediaStorageService.deleteShopMedia(shopId);

        // Then
        assertEquals(0, mediaStorageService.getAllShopMedia(shopId).size());
    }

    @Test
    void testGetShopMediaStats() {
        // Given
        Long shopId = 1L;
        mediaStorageService.saveItemMedia(shopId, 1L, "test1.jpg", MediaType.IMAGE, "test1.jpg");
        mediaStorageService.saveItemMedia(shopId, 2L, "test2.jpg", MediaType.IMAGE, "test2.jpg");
        mediaStorageService.saveItemMedia(shopId, 3L, "test3.mp4", MediaType.VIDEO, "test3.mp4");
        mediaStorageService.saveItemMedia(shopId, 4L, "test4.mp4", MediaType.VIDEO, "test4.mp4");
        mediaStorageService.saveItemMedia(shopId, 5L, "test5.mp4", MediaType.VIDEO, "test5.mp4");

        // When
        Map<String, Object> stats = mediaStorageService.getShopMediaStats(shopId);

        // Then
        assertEquals(shopId, stats.get("shopId"));
        assertEquals(5, stats.get("totalItems"));
        assertEquals(2, stats.get("imageCount"));
        assertEquals(3, stats.get("videoCount"));
        assertNotNull(stats.get("lastChecked"));
    }

    @Test
    void testGetHealthStatus() {
        // When
        Map<String, Object> health = mediaStorageService.getHealthStatus();

        // Then
        assertEquals("MediaStorageService", health.get("service"));
        assertEquals("UP", health.get("status"));
        assertNotNull(health.get("baseStoragePath"));
        assertNotNull(health.get("timestamp"));
        assertTrue((Boolean) health.get("basePathExists"));
    }

    @Test
    void testUpdateExistingItemMedia() {
        // Given
        Long shopId = 1L;
        Long itemId = 100L;
        
        // Save initial media
        mediaStorageService.saveItemMedia(shopId, itemId, "old-url.jpg", MediaType.IMAGE, "old.jpg");
        
        // When - Update with new media
        mediaStorageService.saveItemMedia(shopId, itemId, "new-url.mp4", MediaType.VIDEO, "new.mp4");

        // Then
        Optional<Map<String, Object>> result = mediaStorageService.getItemMedia(shopId, itemId);
        assertTrue(result.isPresent());
        
        Map<String, Object> mediaData = result.get();
        assertEquals("new-url.mp4", mediaData.get("mediaUrl"));
        assertEquals("VIDEO", mediaData.get("mediaType"));
        assertEquals("new.mp4", mediaData.get("fileName"));
    }

    @Test
    void testMultipleShopsIsolation() {
        // Given
        Long shop1Id = 1L;
        Long shop2Id = 2L;
        Long itemId = 100L;
        
        // Save media for both shops with same item ID
        mediaStorageService.saveItemMedia(shop1Id, itemId, "shop1-item.jpg", MediaType.IMAGE, "shop1.jpg");
        mediaStorageService.saveItemMedia(shop2Id, itemId, "shop2-item.mp4", MediaType.VIDEO, "shop2.mp4");

        // When & Then
        Optional<Map<String, Object>> shop1Media = mediaStorageService.getItemMedia(shop1Id, itemId);
        Optional<Map<String, Object>> shop2Media = mediaStorageService.getItemMedia(shop2Id, itemId);
        
        assertTrue(shop1Media.isPresent());
        assertTrue(shop2Media.isPresent());
        
        assertEquals("shop1-item.jpg", shop1Media.get().get("mediaUrl"));
        assertEquals("shop2-item.mp4", shop2Media.get().get("mediaUrl"));
        assertEquals("IMAGE", shop1Media.get().get("mediaType"));
        assertEquals("VIDEO", shop2Media.get().get("mediaType"));
        
        // Verify shop isolation
        assertEquals(1, mediaStorageService.getAllShopMedia(shop1Id).size());
        assertEquals(1, mediaStorageService.getAllShopMedia(shop2Id).size());
    }
}

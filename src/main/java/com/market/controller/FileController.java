package com.market.controller;

import com.market.service.FileStorageService;
import com.market.service.ItemService;
import com.market.service.ShopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService, ShopService shopService, ItemService itemService) {
        this.fileStorageService = fileStorageService;
    }

    // ==================== UPLOAD ENDPOINTS ====================

    @PostMapping("/upload/shop-image")
    public ResponseEntity<Map<String, String>> uploadShopProfileImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("shopId") Long shopId) {

        logger.info("Uploading shop profile image for shop ID: {}", shopId);

        try {
            String imageKey = fileStorageService.storeShopProfileImage(file, shopId);

            Map<String, String> response = new HashMap<>();
            response.put("imageKey", imageKey);
            response.put("message", "Shop profile image uploaded successfully");

            logger.info("Successfully uploaded shop profile image: {}", imageKey);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to upload shop profile image for shop ID: {}", shopId, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/upload/item-media")
    public ResponseEntity<Map<String, String>> uploadItemMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("shopId") Long shopId,
            @RequestParam("itemId") Long itemId) {

        logger.info("Uploading item media for shop ID: {}, item ID: {}", shopId, itemId);

        try {
            String mediaKey = fileStorageService.storeItemMedia(file, shopId, itemId);

            Map<String, String> response = new HashMap<>();
            response.put("mediaKey", mediaKey);
            response.put("message", "Item media uploaded successfully");

            logger.info("Successfully uploaded item media: {}", mediaKey);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to upload item media for shop ID: {}, item ID: {}", shopId, itemId, e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to upload media: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}

package com.market.controller;

import com.market.service.FileStorageService;
import com.market.service.ItemService;
import com.market.service.ShopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.io.FileNotFoundException;
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

    @PostMapping("/upload/item-image")
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

    // ==================== FETCH ENDPOINTS ====================

    @GetMapping("/fetch/image")
    public ResponseEntity<?> fetchImage(@RequestParam("imageKey") String imageKey) {
        logger.info("Fetching image with key: {}", imageKey);
        return getFileResource(imageKey);
    }


    /**
     * Helper method to handle file fetching and error responses.
     *
     * @param relativePath The relative path of the file to fetch.
     * @return A ResponseEntity containing the file or an error status.
     */
    private ResponseEntity<?> getFileResource(String relativePath) {
        try {
            Resource fileResource = fileStorageService.loadImageAsResource(relativePath);
            String contentType = fileStorageService.getContentType(fileResource);
            String filename = fileResource.getFilename();

            logger.info("Successfully fetched file: {}", filename);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(fileResource);

        } catch (FileNotFoundException e) {
            String errorMessage = "File not found for relative path: " + relativePath;
            logger.warn(errorMessage);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("errorMessage", errorMessage));
        } catch (Exception e) {
            String errorMessage = "Failed to fetch file for relative path: " + relativePath;
            logger.error(errorMessage, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("errorMessage", "Failed to fetch file: " + e.getMessage()));
        }
    }

}

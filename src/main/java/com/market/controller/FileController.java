package com.market.controller;

import com.market.model.Item;
import com.market.model.Shop;
import com.market.service.FileStorageService;
import com.market.service.ItemService;
import com.market.service.ShopService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/files")
public class FileController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private static final int DEFAULT_CHUNK_SIZE = 1024 * 1024; // 1MB
    private static final Pattern RANGE_PATTERN = Pattern.compile("bytes=(?<start>\\d+)-(?<end>\\d*)");
    
    private final FileStorageService fileStorageService;
    
    @Autowired
    private ShopService shopService;
    
    @Autowired
    private ItemService itemService;
    
    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }
    
    // ==================== UPLOAD ENDPOINTS ====================
    
    @PostMapping("/upload/shop-profile")
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
    
    // ==================== STREAMING ENDPOINTS ====================
    
    @GetMapping("/shop/{shopId}/profile")
    public ResponseEntity<Resource> getShopProfileById(@PathVariable Long shopId, HttpServletRequest request) {
        logger.debug("Streaming shop profile: shopId={}", shopId);
        
        try {
            Shop shop = shopService.getShopById(shopId);
            
            if (shop.getImageKey() == null || shop.getImageKey().isEmpty()) {
                logger.debug("Shop {} has no profile image", shopId);
                return ResponseEntity.notFound().build();
            }
            
            return streamFile(shop.getImageKey(), request, "shop-profile-" + shopId);
            
        } catch (Exception e) {
            logger.error("Error streaming shop profile: shopId={}", shopId, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/item/{itemId}/media")
    public ResponseEntity<Resource> getItemMediaById(@PathVariable Long itemId, HttpServletRequest request) {
        logger.debug("Streaming item media: itemId={}", itemId);
        
        try {
            Item item = itemService.getItemById(itemId);
            
            if (item.getMediaUrl() == null || item.getMediaUrl().isEmpty()) {
                logger.debug("Item {} has no media", itemId);
                return ResponseEntity.notFound().build();
            }
            
            return streamFile(item.getMediaUrl(), request, "item-media-" + itemId);
            
        } catch (Exception e) {
            logger.error("Error streaming item media: itemId={}", itemId, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/shop-profiles/{shopId}/{filename:.+}")
    public ResponseEntity<Resource> getShopProfileByFilename(@PathVariable Long shopId, 
                                                           @PathVariable String filename, 
                                                           HttpServletRequest request) {
        logger.debug("Streaming shop profile by filename: shopId={}, filename={}", shopId, filename);
        
        try {
            String relativePath = "shop-profiles/shop-" + shopId + "/" + filename;
            return streamFile(relativePath, request, filename);
        } catch (Exception e) {
            logger.error("Error streaming shop profile by filename: shopId={}, filename={}", shopId, filename, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/items/{shopId}/{filename:.+}")
    public ResponseEntity<Resource> getItemMediaByFilename(@PathVariable Long shopId, 
                                                         @PathVariable String filename, 
                                                         HttpServletRequest request) {
        logger.debug("Streaming item media by filename: shopId={}, filename={}", shopId, filename);
        
        try {
            String relativePath = "items/shop-" + shopId + "/" + filename;
            return streamFile(relativePath, request, filename);
        } catch (Exception e) {
            logger.error("Error streaming item media by filename: shopId={}, filename={}", shopId, filename, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    // ==================== CORE STREAMING METHOD ====================
    
    private ResponseEntity<Resource> streamFile(String relativePath, HttpServletRequest request, String displayName) {
        try {
            // Check if file exists
            if (!fileStorageService.fileExists(relativePath)) {
                logger.warn("File not found: {}", relativePath);
                return ResponseEntity.notFound().build();
            }
            
            long fileSize = fileStorageService.getFileSize(relativePath);
            String rangeHeader = request.getHeader(HttpHeaders.RANGE);
            MediaType contentType = determineContentType(relativePath);
            
            logger.debug("Streaming file: path={}, size={}, range={}, contentType={}", 
                        relativePath, fileSize, rangeHeader, contentType);
            
            // Handle range requests (for video streaming and resume support)
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                return handleRangeRequest(relativePath, rangeHeader, fileSize, contentType, displayName);
            } else {
                return handleFullFileRequest(relativePath, fileSize, contentType, displayName);
            }
            
        } catch (IOException e) {
            logger.error("Error streaming file: {}", relativePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private ResponseEntity<Resource> handleRangeRequest(String relativePath, String rangeHeader, 
                                                       long fileSize, MediaType contentType, String displayName) {
        try {
            Matcher matcher = RANGE_PATTERN.matcher(rangeHeader);
            if (!matcher.matches()) {
                logger.warn("Invalid range header: {}", rangeHeader);
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                        .build();
            }
            
            long start = Long.parseLong(matcher.group("start"));
            String endGroup = matcher.group("end");
            long end = endGroup.isEmpty() ? fileSize - 1 : Long.parseLong(endGroup);
            
            // Validate range
            if (start > end || start < 0 || end >= fileSize) {
                logger.warn("Invalid range: start={}, end={}, fileSize={}", start, end, fileSize);
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                        .build();
            }
            
            // Optimize chunk size for large ranges
            long rangeSize = end - start + 1;
            if (rangeSize > DEFAULT_CHUNK_SIZE) {
                end = start + DEFAULT_CHUNK_SIZE - 1;
                if (end >= fileSize) {
                    end = fileSize - 1;
                }
            }
            
            byte[] data = fileStorageService.readFileChunk(relativePath, start, end);
            long contentLength = end - start + 1;
            
            logger.debug("Serving range: start={}, end={}, contentLength={}", start, end, contentLength);
            
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(data));
            
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.CONTENT_TYPE, contentType.toString())
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                    .header(HttpHeaders.CONTENT_RANGE, String.format("bytes %d-%d/%d", start, end, fileSize))
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + displayName + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, getCacheControl(contentType))
                    .body(resource);
                    
        } catch (IOException e) {
            logger.error("Error handling range request for file: {}", relativePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private ResponseEntity<Resource> handleFullFileRequest(String relativePath, long fileSize, 
                                                         MediaType contentType, String displayName) {
        try {
            Resource resource = fileStorageService.getFileResource(relativePath);
            
            logger.debug("Serving full file: size={}, contentType={}", fileSize, contentType);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType.toString())
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize))
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + displayName + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, getCacheControl(contentType))
                    .body(resource);
                    
        } catch (IOException e) {
            logger.error("Error handling full file request for: {}", relativePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    private String getCacheControl(MediaType contentType) {
        if (contentType.getType().equals("video")) {
            return "public, max-age=86400, immutable"; // 24 hours for videos
        } else if (contentType.getType().equals("image")) {
            return "public, max-age=3600, immutable"; // 1 hour for images
        } else {
            return "public, max-age=1800"; // 30 minutes for other files
        }
    }
    
    private MediaType determineContentType(String relativePath) {
        String filename = Path.of(relativePath).getFileName().toString();
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();

        switch (extension) {
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG;
            case "png":
                return MediaType.IMAGE_PNG;
            case "gif":
                return MediaType.IMAGE_GIF;
            case "webp":
                return MediaType.valueOf("image/webp");
            case "mp4":
                return MediaType.valueOf("video/mp4");
            case "avi":
                return MediaType.valueOf("video/avi");
            case "mov":
                return MediaType.valueOf("video/quicktime");
            case "wmv":
                return MediaType.valueOf("video/x-ms-wmv");
            case "flv":
                return MediaType.valueOf("video/x-flv");
            case "webm":
                return MediaType.valueOf("video/webm");
            case "mkv":
                return MediaType.valueOf("video/x-matroska");
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}

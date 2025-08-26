package com.market.service.storage;

import com.market.model.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Local file system storage strategy implementation.
 * Stores files on the local disk using the existing file storage logic.
 */
@Component("localFileStorageStrategy")
public class LocalFileStorageStrategy implements FileStorageStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalFileStorageStrategy.class);
    
    @Value("${file.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${file.upload.shop-profiles:shop-profiles}")
    private String shopProfilesDir;
    
    @Value("${file.upload.items:items}")
    private String itemsDir;
    
    @Override
    public String storeShopProfileImage(MultipartFile file, Long shopId) throws IOException {
        validateImageFile(file);
        
        // Create directory structure: uploads/shop-profiles/shop-{id}/
        Path shopProfilePath = Paths.get(uploadDir, shopProfilesDir, "shop-" + shopId);
        Files.createDirectories(shopProfilePath);
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = "profile-" + UUID.randomUUID().toString() + fileExtension;
        
        // Store file
        Path targetLocation = shopProfilePath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        logger.info("Stored shop profile image using local storage: shopId={}, filename={}", shopId, uniqueFilename);
        
        // Return relative path for storage in database
        return shopProfilesDir + "/shop-" + shopId + "/" + uniqueFilename;
    }
    
    @Override
    public String storeItemMedia(MultipartFile file, Long shopId, Long itemId) throws IOException {
        validateMediaFile(file);

        // Create directory structure: uploads/items/shop-{shopId}/
        Path itemsPath = Paths.get(uploadDir, itemsDir, "shop-" + shopId);
        Files.createDirectories(itemsPath);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = "item-" + itemId + "-" + UUID.randomUUID().toString() + fileExtension;

        // Store file
        Path targetLocation = itemsPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        logger.info("Stored item media using local storage: shopId={}, itemId={}, filename={}", shopId, itemId, uniqueFilename);

        // Return relative path for storage in database
        return itemsDir + "/shop-" + shopId + "/" + uniqueFilename;
    }
    
    @Override
    public void deleteFile(String storageIdentifier) throws IOException {
        Path filePath = Paths.get(uploadDir, storageIdentifier);
        if (Files.deleteIfExists(filePath)) {
            logger.info("Deleted file using local storage: {}", storageIdentifier);
        } else {
            logger.warn("File not found for deletion: {}", storageIdentifier);
        }
    }
    
    @Override
    public boolean fileExists(String storageIdentifier) {
        Path filePath = Paths.get(uploadDir, storageIdentifier);
        return Files.exists(filePath);
    }
    
    @Override
    public Resource getFileResource(String storageIdentifier) throws IOException {
        Path filePath = Paths.get(uploadDir, storageIdentifier);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("File not found or not readable: " + storageIdentifier);
        }

        return resource;
    }
    
    @Override
    public long getFileSize(String storageIdentifier) throws IOException {
        Path filePath = Paths.get(uploadDir, storageIdentifier);
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + storageIdentifier);
        }
        return Files.size(filePath);
    }
    
    @Override
    public byte[] readFileChunk(String storageIdentifier, long start, long end) throws IOException {
        Path filePath = Paths.get(uploadDir, storageIdentifier);

        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + storageIdentifier);
        }

        try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "r")) {
            long fileSize = file.length();

            // Validate range
            if (start < 0 || start >= fileSize) {
                throw new IOException("Invalid start position: " + start);
            }

            if (end < 0 || end >= fileSize) {
                end = fileSize - 1;
            }

            if (start > end) {
                throw new IOException("Invalid range: start > end");
            }

            int chunkSize = (int) (end - start + 1);
            byte[] buffer = new byte[chunkSize];

            file.seek(start);
            int bytesRead = file.read(buffer);

            if (bytesRead != chunkSize) {
                // Adjust buffer size if we read less than expected
                byte[] actualBuffer = new byte[bytesRead];
                System.arraycopy(buffer, 0, actualBuffer, 0, bytesRead);
                return actualBuffer;
            }

            return buffer;
        }
    }
    
    @Override
    public boolean supportsStreaming(String storageIdentifier) {
        String extension = getFileExtension(storageIdentifier).toLowerCase();
        // Video files that support streaming
        return extension.matches("\\.(mp4|webm|mov|avi|mkv)");
    }
    
    @Override
    public int getOptimalChunkSize(String storageIdentifier) {
        String extension = getFileExtension(storageIdentifier).toLowerCase();

        if (extension.matches("\\.(mp4|webm|mov)")) {
            return 1024 * 1024; // 1MB for modern video formats
        } else if (extension.matches("\\.(avi|mkv|wmv)")) {
            return 512 * 1024; // 512KB for older formats
        } else {
            return 64 * 1024; // 64KB for images and other files
        }
    }
    
    @Override
    public MediaType determineMediaType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return MediaType.IMAGE;
            } else if (contentType.startsWith("video/")) {
                return MediaType.VIDEO;
            }
        }
        return MediaType.IMAGE; // Default to image
    }
    
    @Override
    public String getStrategyName() {
        return "local";
    }
    
    @Override
    public boolean isAvailable() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            return Files.isWritable(uploadPath);
        } catch (IOException e) {
            logger.error("Local storage is not available", e);
            return false;
        }
    }
    
    private void validateImageFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("File must be an image");
        }
        
        // Check file size (max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IOException("File size must be less than 5MB");
        }
        
        // Check allowed extensions
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IOException("Invalid filename");
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            throw new IOException("Only JPG, JPEG, PNG, GIF, and WebP files are allowed");
        }
    }

    private void validateMediaFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.startsWith("video/"))) {
            throw new IOException("File must be an image or video");
        }

        // Check file size (max 50MB for videos, 5MB for images)
        long maxSize = contentType.startsWith("video/") ? 50 * 1024 * 1024 : 5 * 1024 * 1024; // 50MB for video, 5MB for image
        if (file.getSize() > maxSize) {
            String sizeLimit = contentType.startsWith("video/") ? "50MB" : "5MB";
            throw new IOException("File size must be less than " + sizeLimit);
        }

        // Check allowed extensions
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IOException("Invalid filename");
        }

        String extension = getFileExtension(filename).toLowerCase();
        if (contentType.startsWith("image/")) {
            if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
                throw new IOException("Only JPG, JPEG, PNG, GIF, and WebP image files are allowed");
            }
        } else if (contentType.startsWith("video/")) {
            if (!extension.matches("\\.(mp4|avi|mov|wmv|flv|webm|mkv)")) {
                throw new IOException("Only MP4, AVI, MOV, WMV, FLV, WebM, and MKV video files are allowed");
            }
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}

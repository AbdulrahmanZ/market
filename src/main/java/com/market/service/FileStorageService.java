package com.market.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${file.upload.shop-profiles:shop-profiles}")
    private String shopProfilesDir;

    @Value("${file.upload.items:items}")
    private String itemsDir;

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

        // Return relative path for storage in database
        return shopProfilesDir + "/shop-" + shopId + "/" + uniqueFilename;
    }

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

        // Return relative path for storage in database
        return itemsDir + "/shop-" + shopId + "/" + uniqueFilename;
    }

    public void deleteFile(String relativePath) {
        try {
            Path filePath = Paths.get(uploadDir, relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but don't throw exception
            System.err.println("Failed to delete file: " + relativePath + " - " + e.getMessage());
        }
    }

    public boolean fileExists(String relativePath) {
        Path filePath = Paths.get(uploadDir, relativePath);
        return Files.exists(filePath);
    }

    public Path getFilePath(String relativePath) {
        return Paths.get(uploadDir, relativePath);
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

    public com.market.model.MediaType determineMediaType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return com.market.model.MediaType.IMAGE;
            } else if (contentType.startsWith("video/")) {
                return com.market.model.MediaType.VIDEO;
            }
        }
        return com.market.model.MediaType.IMAGE; // Default to image
    }

    // ==================== STREAMING SUPPORT METHODS ====================

    /**
     * Get file size for streaming support
     */
    public long getFileSize(String relativePath) throws IOException {
        Path filePath = Paths.get(uploadDir, relativePath);
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + relativePath);
        }
        return Files.size(filePath);
    }

    /**
     * Get file resource for streaming
     */
    public Resource getFileResource(String relativePath) throws IOException {
        Path filePath = Paths.get(uploadDir, relativePath);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("File not found or not readable: " + relativePath);
        }

        return resource;
    }

    /**
     * Read file chunk for range requests (streaming)
     */
    public byte[] readFileChunk(String relativePath, long start, long end) throws IOException {
        Path filePath = Paths.get(uploadDir, relativePath);

        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + relativePath);
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

    /**
     * Check if file supports streaming (mainly for videos)
     */
    public boolean supportsStreaming(String relativePath) {
        String extension = getFileExtension(relativePath).toLowerCase();
        // Video files that support streaming
        return extension.matches("\\.(mp4|webm|mov|avi|mkv)");
    }

    /**
     * Get optimal chunk size for streaming based on file type
     */
    public int getOptimalChunkSize(String relativePath) {
        String extension = getFileExtension(relativePath).toLowerCase();

        if (extension.matches("\\.(mp4|webm|mov)")) {
            return 1024 * 1024; // 1MB for modern video formats
        } else if (extension.matches("\\.(avi|mkv|wmv)")) {
            return 512 * 1024; // 512KB for older formats
        } else {
            return 64 * 1024; // 64KB for images and other files
        }
    }
}

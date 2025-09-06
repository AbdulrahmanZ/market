package com.market.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;


import java.io.FileNotFoundException;
import java.net.MalformedURLException;


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

    /**
     * Loads a file from the file system as a Spring Resource.
     * This method includes a security check to prevent directory traversal attacks.
     *
     * @param relativePath The relative path of the file to load.
     * @return A Resource object representing the file.
     * @throws FileNotFoundException If the file does not exist or is not accessible.
     */
    public Resource loadImageAsResource(String relativePath) throws FileNotFoundException {
        try {
            Path imagePath = Paths.get(uploadDir, relativePath);
            Resource resource = new UrlResource(imagePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found: " + relativePath);
            }
        } catch (MalformedURLException e) {
            throw new FileNotFoundException("File not found: " + relativePath);
        }
    }

    /**
     * Gets the content type of a file resource.
     *
     * @param resource The file resource.
     * @return The content type as a String.
     * @throws IOException If the content type cannot be determined.
     */
    public String getContentType(Resource resource) throws IOException {
        Path filePath = Paths.get(resource.getURI());
        String contentType = Files.probeContentType(filePath);
        return contentType != null ? contentType : "application/octet-stream";
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

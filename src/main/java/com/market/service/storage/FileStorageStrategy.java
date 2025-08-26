package com.market.service.storage;

import com.market.model.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Strategy interface for file storage implementations.
 * This allows switching between different storage providers (local disk, Google Drive, etc.)
 * at runtime without changing the client code.
 */
public interface FileStorageStrategy {
    
    /**
     * Store a shop profile image
     * @param file The file to store
     * @param shopId The shop ID
     * @return The storage identifier/path for the stored file
     * @throws IOException if storage fails
     */
    String storeShopProfileImage(MultipartFile file, Long shopId) throws IOException;
    
    /**
     * Store an item media file
     * @param file The file to store
     * @param shopId The shop ID
     * @param itemId The item ID
     * @return The storage identifier/path for the stored file
     * @throws IOException if storage fails
     */
    String storeItemMedia(MultipartFile file, Long shopId, Long itemId) throws IOException;
    
    /**
     * Delete a file by its storage identifier
     * @param storageIdentifier The storage identifier/path of the file to delete
     * @throws IOException if deletion fails
     */
    void deleteFile(String storageIdentifier) throws IOException;
    
    /**
     * Check if a file exists
     * @param storageIdentifier The storage identifier/path to check
     * @return true if the file exists, false otherwise
     */
    boolean fileExists(String storageIdentifier);
    
    /**
     * Get a file as a Resource for streaming/download
     * @param storageIdentifier The storage identifier/path of the file
     * @return Resource object for the file
     * @throws IOException if the file cannot be accessed
     */
    Resource getFileResource(String storageIdentifier) throws IOException;
    
    /**
     * Get file size in bytes
     * @param storageIdentifier The storage identifier/path of the file
     * @return File size in bytes
     * @throws IOException if the file cannot be accessed
     */
    long getFileSize(String storageIdentifier) throws IOException;
    
    /**
     * Read a chunk of a file for streaming support
     * @param storageIdentifier The storage identifier/path of the file
     * @param start Start position in bytes
     * @param end End position in bytes
     * @return Byte array containing the file chunk
     * @throws IOException if the file cannot be accessed
     */
    byte[] readFileChunk(String storageIdentifier, long start, long end) throws IOException;
    
    /**
     * Check if the file supports streaming
     * @param storageIdentifier The storage identifier/path of the file
     * @return true if streaming is supported, false otherwise
     */
    boolean supportsStreaming(String storageIdentifier);
    
    /**
     * Get optimal chunk size for streaming
     * @param storageIdentifier The storage identifier/path of the file
     * @return Optimal chunk size in bytes
     */
    int getOptimalChunkSize(String storageIdentifier);
    
    /**
     * Determine the media type of a file
     * @param file The file to analyze
     * @return MediaType enum value
     */
    MediaType determineMediaType(MultipartFile file);
    
    /**
     * Get the strategy name/identifier
     * @return Strategy name
     */
    String getStrategyName();
    
    /**
     * Check if the strategy is available/configured
     * @return true if the strategy is available, false otherwise
     */
    boolean isAvailable();
}

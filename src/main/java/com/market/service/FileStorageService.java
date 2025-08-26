package com.market.service;

import com.market.service.storage.FileStorageContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class FileStorageService {
    
    @Autowired
    private FileStorageContext fileStorageContext;
    
    public String storeShopProfileImage(MultipartFile file, Long shopId) throws IOException {
        return fileStorageContext.storeShopProfileImage(file, shopId);
    }
    
    public String storeItemMedia(MultipartFile file, Long shopId, Long itemId) throws IOException {
        return fileStorageContext.storeItemMedia(file, shopId, itemId);
    }
    
    public void deleteFile(String relativePath) throws IOException {
        fileStorageContext.deleteFile(relativePath);
    }
    
    public boolean fileExists(String relativePath) {
        return fileStorageContext.fileExists(relativePath);
    }
    
    public Path getFilePath(String relativePath) {
        // This method is kept for backward compatibility but may not work with all strategies
        // For Google Drive, this would not be applicable
        throw new UnsupportedOperationException("getFilePath is not supported with the current storage strategy");
    }
    


    public com.market.model.MediaType determineMediaType(MultipartFile file) {
        return fileStorageContext.determineMediaType(file);
    }

    // ==================== STREAMING SUPPORT METHODS ====================

    /**
     * Get file size for streaming support
     */
    public long getFileSize(String relativePath) throws IOException {
        return fileStorageContext.getFileSize(relativePath);
    }

    /**
     * Get file resource for streaming
     */
    public Resource getFileResource(String relativePath) throws IOException {
        return fileStorageContext.getFileResource(relativePath);
    }

    /**
     * Read file chunk for range requests (streaming)
     */
    public byte[] readFileChunk(String relativePath, long start, long end) throws IOException {
        return fileStorageContext.readFileChunk(relativePath, start, end);
    }

    /**
     * Check if file supports streaming (mainly for videos)
     */
    public boolean supportsStreaming(String relativePath) {
        return fileStorageContext.supportsStreaming(relativePath);
    }

    /**
     * Get optimal chunk size for streaming based on file type
     */
    public int getOptimalChunkSize(String relativePath) {
        return fileStorageContext.getOptimalChunkSize(relativePath);
    }
}

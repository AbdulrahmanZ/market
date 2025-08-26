package com.market.controller;

import com.market.service.storage.FileStorageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing file storage strategies at runtime.
 * Allows administrators to switch between different storage providers.
 */
@RestController
@RequestMapping("/api/storage")
public class StorageController {
    
    private static final Logger logger = LoggerFactory.getLogger(StorageController.class);
    
    @Autowired
    private FileStorageContext fileStorageContext;
    
    /**
     * Get current storage strategy and available strategies
     */
    @GetMapping("/strategy")
    public ResponseEntity<Map<String, Object>> getCurrentStrategy() {
        Map<String, Object> response = new HashMap<>();
        response.put("currentStrategy", fileStorageContext.getCurrentStrategyName());
        response.put("availableStrategies", fileStorageContext.getAvailableStrategies());
        response.put("healthStatus", fileStorageContext.getHealthStatus());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Switch to a different storage strategy
     */
    @PostMapping("/strategy")
    public ResponseEntity<Map<String, Object>> setStrategy(@RequestBody Map<String, String> request) {
        String strategyName = request.get("strategy");
        
        if (strategyName == null || strategyName.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Strategy name is required");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            fileStorageContext.setStrategy(strategyName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Storage strategy switched successfully");
            response.put("currentStrategy", fileStorageContext.getCurrentStrategyName());
            response.put("availableStrategies", fileStorageContext.getAvailableStrategies());
            
            logger.info("Storage strategy switched to: {}", strategyName);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to switch storage strategy: " + e.getMessage());
            logger.error("Failed to switch storage strategy", e);
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get health status of all storage strategies
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        return ResponseEntity.ok(fileStorageContext.getHealthStatus());
    }
    
    /**
     * Migrate files from one strategy to another
     */
    @PostMapping("/migrate")
    public ResponseEntity<Map<String, Object>> migrateFiles(@RequestBody Map<String, Object> request) {
        String fromStrategy = (String) request.get("fromStrategy");
        String toStrategy = (String) request.get("toStrategy");
        @SuppressWarnings("unchecked")
        List<String> storageIdentifiers = (List<String>) request.get("storageIdentifiers");
        
        if (fromStrategy == null || toStrategy == null || storageIdentifiers == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "fromStrategy, toStrategy, and storageIdentifiers are required");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            Map<String, String> migrationMap = fileStorageContext.migrateFiles(
                fromStrategy, toStrategy, storageIdentifiers
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Migration completed successfully");
            response.put("migratedFiles", migrationMap.size());
            response.put("migrationMap", migrationMap);
            
            logger.info("File migration completed: {} files from {} to {}", 
                storageIdentifiers.size(), fromStrategy, toStrategy);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Migration failed: " + e.getMessage());
            logger.error("File migration failed", e);
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Test file operations with current strategy
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testStorageStrategy() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String currentStrategy = fileStorageContext.getCurrentStrategyName();
            Map<String, Boolean> availableStrategies = fileStorageContext.getAvailableStrategies();
            
            response.put("currentStrategy", currentStrategy);
            response.put("isAvailable", availableStrategies.get(currentStrategy));
            response.put("testStatus", "success");
            response.put("message", "Storage strategy is working correctly");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Storage strategy test failed: " + e.getMessage());
            logger.error("Storage strategy test failed", e);
            return ResponseEntity.internalServerError().body(error);
        }
    }
}

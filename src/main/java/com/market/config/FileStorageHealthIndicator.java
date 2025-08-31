package com.market.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Component
public class FileStorageHealthIndicator implements HealthIndicator {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${media.storage.base-path:media-storage}")
    private String mediaStoragePath;

    @Override
    public Health health() {
        try {
            Map<String, Object> details = new HashMap<>();
            
            // Check uploads directory
            Path uploadsPath = Paths.get(uploadDir);
            if (Files.exists(uploadsPath)) {
                File uploadsDir = uploadsPath.toFile();
                long freeSpace = uploadsDir.getFreeSpace();
                long totalSpace = uploadsDir.getTotalSpace();
                long usedSpace = totalSpace - freeSpace;
                double usagePercentage = (double) usedSpace / totalSpace * 100;
                
                details.put("uploadsDirectory", uploadsPath.toAbsolutePath().toString());
                details.put("uploadsFreeSpace", formatBytes(freeSpace));
                details.put("uploadsUsedSpace", formatBytes(usedSpace));
                details.put("uploadsTotalSpace", formatBytes(totalSpace));
                details.put("uploadsUsagePercentage", String.format("%.2f%%", usagePercentage));
                
                // Check if disk space is running low (less than 10% free)
                if (usagePercentage > 90) {
                    return Health.down()
                        .withDetail("warning", "Disk space is running low")
                        .withDetails(details)
                        .build();
                }
            } else {
                details.put("uploadsDirectory", "Directory does not exist: " + uploadsPath.toAbsolutePath());
            }
            
            // Check media storage directory
            Path mediaPath = Paths.get(mediaStoragePath);
            if (Files.exists(mediaPath)) {
                File mediaDir = mediaPath.toFile();
                long freeSpace = mediaDir.getFreeSpace();
                long totalSpace = mediaDir.getTotalSpace();
                long usedSpace = totalSpace - freeSpace;
                double usagePercentage = (double) usedSpace / totalSpace * 100;
                
                details.put("mediaStorageDirectory", mediaPath.toAbsolutePath().toString());
                details.put("mediaFreeSpace", formatBytes(freeSpace));
                details.put("mediaUsedSpace", formatBytes(usedSpace));
                details.put("mediaTotalSpace", formatBytes(totalSpace));
                details.put("mediaUsagePercentage", String.format("%.2f%%", usagePercentage));
                
                // Check if disk space is running low (less than 10% free)
                if (usagePercentage > 90) {
                    return Health.down()
                        .withDetail("warning", "Media storage disk space is running low")
                        .withDetails(details)
                        .build();
                }
            } else {
                details.put("mediaStorageDirectory", "Directory does not exist: " + mediaPath.toAbsolutePath());
            }
            
            return Health.up()
                .withDetails(details)
                .build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", "File storage health check failed: " + e.getMessage())
                .build();
        }
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}

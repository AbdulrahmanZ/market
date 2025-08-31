package com.market.config;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class MarketInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> details = new HashMap<>();
        
        // Application details
        details.put("name", "Market Application");
        details.put("description", "A marketplace application for shops and items");
        details.put("version", "1.0.0");
        details.put("environment", "Development");
        
        // Current timestamp
        details.put("currentTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Features
        Map<String, Object> features = new HashMap<>();
        features.put("userManagement", true);
        features.put("shopManagement", true);
        features.put("itemManagement", true);
        features.put("fileUpload", true);
        features.put("jwtAuthentication", true);
        features.put("roleBasedAccess", true);
        details.put("features", features);
        
        // Technology stack
        Map<String, Object> techStack = new HashMap<>();
        techStack.put("framework", "Spring Boot 3.2.0");
        techStack.put("java", "Java 17");
        techStack.put("database", "MySQL");
        techStack.put("security", "Spring Security + JWT");
        techStack.put("monitoring", "Spring Boot Actuator");
        details.put("technologyStack", techStack);
        
        builder.withDetails(details);
    }
}

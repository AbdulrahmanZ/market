package com.market.config;

import com.market.repository.UserRepository;
import com.market.repository.ShopRepository;
import com.market.repository.ItemRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableScheduling
public class MarketMetricsConfig {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Counter userRegistrationCounter;
    private Counter shopCreationCounter;
    private Counter itemCreationCounter;
    private Counter loginAttemptsCounter;

    @PostConstruct
    public void init() {
        // Initialize counters
        userRegistrationCounter = Counter.builder("market.user.registrations")
            .description("Total number of user registrations")
            .register(meterRegistry);

        shopCreationCounter = Counter.builder("market.shop.creations")
            .description("Total number of shop creations")
            .register(meterRegistry);

        itemCreationCounter = Counter.builder("market.item.creations")
            .description("Total number of item creations")
            .register(meterRegistry);

        loginAttemptsCounter = Counter.builder("market.auth.login.attempts")
            .description("Total number of login attempts")
            .register(meterRegistry);

        // Initialize gauges for current counts
        Gauge.builder("market.users.total", userRepository, UserRepository::count)
            .description("Current total number of users")
            .register(meterRegistry);

        Gauge.builder("market.shops.total", shopRepository, ShopRepository::count)
            .description("Current total number of shops")
            .register(meterRegistry);

        Gauge.builder("market.items.total", itemRepository, ItemRepository::count)
            .description("Current total number of items")
            .register(meterRegistry);

        Gauge.builder("market.shops.active", shopRepository, 
            repo -> repo.countByIsActiveTrue())
            .description("Current number of active shops")
            .register(meterRegistry);
    }

    // Methods to increment counters (call these from your services)
    public void incrementUserRegistration() {
        userRegistrationCounter.increment();
    }

    public void incrementShopCreation() {
        shopCreationCounter.increment();
    }

    public void incrementItemCreation() {
        itemCreationCounter.increment();
    }

    public void incrementLoginAttempts() {
        loginAttemptsCounter.increment();
    }

    // Scheduled task to update metrics every 5 minutes
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void updateMetrics() {
        // The gauges will automatically update when accessed
        // This method can be used for additional metric calculations if needed
    }
}

package com.market.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            // Test basic connectivity
            if (connection.isValid(5)) {
                // Test with a simple query
                String result = jdbcTemplate.queryForObject("SELECT 'OK' as status", String.class);
                
                Map<String, Object> details = new HashMap<>();
                details.put("database", connection.getMetaData().getDatabaseProductName());
                details.put("version", connection.getMetaData().getDatabaseProductVersion());
                details.put("url", connection.getMetaData().getURL());
                details.put("username", connection.getMetaData().getUserName());
                details.put("driver", connection.getMetaData().getDriverName());
                details.put("driverVersion", connection.getMetaData().getDriverVersion());
                
                return Health.up()
                    .withDetails(details)
                    .build();
            } else {
                return Health.down()
                    .withDetail("error", "Database connection is not valid")
                    .build();
            }
        } catch (SQLException e) {
            return Health.down()
                .withDetail("error", "Database connection failed: " + e.getMessage())
                .withDetail("sqlState", e.getSQLState())
                .withDetail("errorCode", e.getErrorCode())
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", "Unexpected error: " + e.getMessage())
                .build();
        }
    }
}

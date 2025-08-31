# Market Application Monitoring with Spring Boot Actuator

This document describes the monitoring and management capabilities of the Market Application using Spring Boot Actuator.

## Overview

Spring Boot Actuator provides production-ready features to monitor and manage your Spring Boot application. It includes built-in endpoints for health checks, metrics, environment info, and more.

## Available Endpoints

### Base URL
All actuator endpoints are available under: `http://localhost:8080/market/actuator`

### 1. Health Check Endpoints

#### `/actuator/health`
- **Purpose**: Overall application health status
- **Response**: UP, DOWN, or UNKNOWN
- **Details**: Shows health of all components including custom health indicators

#### `/actuator/health/{component}`
- **Purpose**: Health status of specific components
- **Available Components**:
  - `db` - Database connectivity
  - `diskSpace` - File storage health
  - `ping` - Basic connectivity

### 2. Application Information

#### `/actuator/info`
- **Purpose**: Application-specific information
- **Response**: Application name, version, features, tech stack, current timestamp

#### `/actuator/env`
- **Purpose**: Environment variables and configuration properties
- **Response**: All environment variables and application properties

### 3. Metrics and Monitoring

#### `/actuator/metrics`
- **Purpose**: List of all available metrics
- **Response**: Available metric names

#### `/actuator/metrics/{metric.name}`
- **Purpose**: Specific metric values
- **Available Metrics**:
  - `market.user.registrations` - Total user registrations
  - `market.shop.creations` - Total shop creations
  - `market.item.creations` - Total item creations
  - `market.auth.login.attempts` - Total login attempts
  - `market.users.total` - Current user count
  - `market.shops.total` - Current shop count
  - `market.items.total` - Current item count
  - `market.shops.active` - Current active shop count

#### `/actuator/prometheus`
- **Purpose**: Prometheus-compatible metrics export
- **Response**: Metrics in Prometheus format for external monitoring systems

### 4. Application Management

#### `/actuator/beans`
- **Purpose**: List all Spring beans
- **Response**: Bean definitions and dependencies

#### `/actuator/mappings`
- **Purpose**: HTTP endpoint mappings
- **Response**: All REST endpoints and their configurations

#### `/actuator/configprops`
- **Purpose**: Configuration properties
- **Response**: All @ConfigurationProperties

#### `/actuator/loggers`
- **Purpose**: Logger configurations
- **Response**: Log levels and configurations

### 5. Application Control

#### `/actuator/shutdown` (POST)
- **Purpose**: Gracefully shutdown the application
- **Method**: POST
- **Warning**: Use with caution in production

## Custom Health Indicators

### 1. Database Health Indicator
- **Component**: `db`
- **Checks**: Database connectivity, connection validity
- **Details**: Database product, version, driver info, connection status

### 2. File Storage Health Indicator
- **Component**: `diskSpace`
- **Checks**: Disk space usage, directory existence
- **Details**: Free space, used space, total space, usage percentage
- **Warnings**: Triggers DOWN status when disk usage > 90%

## Custom Metrics

### Business Metrics
- **User Registrations**: Tracks total user registrations
- **Shop Creations**: Tracks total shop creations
- **Item Creations**: Tracks total item creations
- **Login Attempts**: Tracks both successful and failed login attempts

### System Metrics
- **User Counts**: Real-time user, shop, and item counts
- **Active Shops**: Count of currently active shops
- **JVM Metrics**: Memory usage, garbage collection, thread info
- **System Metrics**: CPU usage, disk I/O, network stats

## Configuration

### application.properties
```properties
# Enable all actuator endpoints
management.endpoints.web.exposure.include=*

# Show detailed health information
management.endpoint.health.show-details=always

# Enable shutdown endpoint
management.endpoint.shutdown.enabled=true

# Custom base path
management.endpoints.web.base-path=/actuator

# Enable various metrics
management.metrics.enable.jvm=true
management.metrics.enable.system=true
management.metrics.enable.process=true
management.metrics.enable.tomcat=true
management.metrics.enable.hikaricp=true
```

## Security Considerations

### Production Recommendations
1. **Limit Endpoint Exposure**: Only expose necessary endpoints
2. **Authentication**: Secure sensitive endpoints
3. **Network Security**: Restrict access to monitoring endpoints
4. **Shutdown Endpoint**: Disable in production or secure with authentication

### Example Secure Configuration
```properties
# Only expose health and info endpoints
management.endpoints.web.exposure.include=health,info,metrics

# Disable shutdown endpoint
management.endpoint.shutdown.enabled=false

# Custom base path for security through obscurity
management.endpoints.web.base-path=/admin/monitoring
```

## Integration with External Monitoring

### Prometheus
- **Endpoint**: `/actuator/prometheus`
- **Format**: Prometheus-compatible metrics
- **Use Case**: Time-series monitoring and alerting

### Grafana
- **Data Source**: Prometheus endpoint
- **Dashboards**: Create custom dashboards for business metrics
- **Alerts**: Set up alerts for critical metrics

### ELK Stack
- **Logs**: Application logs via logback
- **Metrics**: Business metrics via custom endpoints
- **Visualization**: Kibana dashboards

## Usage Examples

### Health Check
```bash
curl http://localhost:8080/market/actuator/health
```

### Get User Count
```bash
curl http://localhost:8080/market/actuator/metrics/market.users.total
```

### Get All Metrics
```bash
curl http://localhost:8080/market/actuator/metrics
```

### Application Info
```bash
curl http://localhost:8080/market/actuator/info
```

## Troubleshooting

### Common Issues
1. **Endpoints Not Accessible**: Check `management.endpoints.web.exposure.include`
2. **Health Checks Failing**: Verify database connectivity and file permissions
3. **Metrics Not Updating**: Ensure services are calling metric increment methods

### Debug Mode
Enable debug logging for actuator:
```properties
logging.level.org.springframework.boot.actuate=DEBUG
```

## Future Enhancements

### Planned Features
1. **Custom Dashboards**: Web-based monitoring interface
2. **Alerting**: Email/SMS notifications for critical issues
3. **Performance Profiling**: Request/response time monitoring
4. **Business Intelligence**: Advanced analytics and reporting

### Integration Possibilities
1. **Slack Notifications**: Health check failures
2. **PagerDuty**: Incident management
3. **New Relic**: APM integration
4. **Datadog**: Infrastructure monitoring

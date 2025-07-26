# MediaStorageService Documentation

## Overview

The `MediaStorageService` is a Spring service that manages media metadata and storage organization for the Market Application. It works alongside `FileStorageService` to provide a complete media management solution.

## Architecture

```
FileStorageService          MediaStorageService
      |                           |
      v                           v
Physical Files              JSON Metadata
(uploads/)                  (media-storage/)
      |                           |
      +-- shop-profiles/          +-- shop-1/
      |   +-- shop-1/             |   +-- items-media.json
      |       +-- profile.jpg     |
      +-- items/                  +-- shop-2/
          +-- shop-1/                 +-- items-media.json
              +-- item-1.jpg
              +-- item-2.mp4
```

## Key Features

### 🗂️ **Metadata Management**
- Stores media information in JSON files organized by shop
- Tracks media URLs, types, filenames, and timestamps
- Provides fast lookup without filesystem scanning

### 📊 **Statistics & Analytics**
- Counts images vs videos per shop
- Tracks total media items
- Provides health status monitoring

### 🔄 **CRUD Operations**
- Save/update item media metadata
- Retrieve individual or bulk media information
- Delete individual items or entire shop media
- Atomic operations with error handling

### 🏪 **Shop Isolation**
- Each shop has its own metadata file
- Complete isolation between shops
- Easy bulk operations per shop

## API Reference

### **Core Methods**

#### Save Item Media
```java
public void saveItemMedia(Long shopId, Long itemId, String mediaUrl, MediaType mediaType, String fileName)
```
- **Purpose**: Save or update media metadata for an item
- **Parameters**:
  - `shopId`: Shop identifier
  - `itemId`: Item identifier  
  - `mediaUrl`: Relative path to media file
  - `mediaType`: IMAGE or VIDEO
  - `fileName`: Original filename
- **Creates**: `media-storage/shop-{shopId}/items-media.json`

#### Get Item Media
```java
public Optional<Map<String, Object>> getItemMedia(Long shopId, Long itemId)
```
- **Purpose**: Retrieve media metadata for a specific item
- **Returns**: Optional containing media information or empty if not found
- **Response Format**:
```json
{
  "itemId": 123,
  "mediaUrl": "items/shop-1/item-123-uuid.jpg",
  "mediaType": "IMAGE",
  "fileName": "product.jpg",
  "lastUpdated": "2025-01-07T10:30:00",
  "shopId": 1
}
```

#### Get All Shop Media
```java
public Map<String, Object> getAllShopMedia(Long shopId)
```
- **Purpose**: Retrieve all media metadata for a shop
- **Returns**: Map with keys like "item_123" containing media objects
- **Use Case**: Bulk operations, media galleries

#### Delete Item Media
```java
public void deleteItemMedia(Long shopId, Long itemId)
```
- **Purpose**: Remove media metadata for a specific item
- **Note**: Only removes metadata, not the actual file

#### Delete Shop Media
```java
public void deleteShopMedia(Long shopId)
```
- **Purpose**: Remove all media metadata for a shop
- **Cleanup**: Also removes empty shop directories

### **Statistics Methods**

#### Get Shop Media Stats
```java
public Map<String, Object> getShopMediaStats(Long shopId)
```
- **Purpose**: Get comprehensive media statistics for a shop
- **Returns**:
```json
{
  "shopId": 1,
  "totalItems": 25,
  "imageCount": 18,
  "videoCount": 7,
  "lastChecked": "2025-01-07T10:30:00"
}
```

#### Health Status
```java
public Map<String, Object> getHealthStatus()
```
- **Purpose**: Check service health and storage accessibility
- **Returns**: Service status, path information, and accessibility checks

## File Structure

### **Storage Organization**
```
media-storage/
├── shop-1/
│   └── items-media.json
├── shop-2/
│   └── items-media.json
└── shop-3/
    └── items-media.json
```

### **JSON File Format**
```json
{
  "item_123": {
    "itemId": 123,
    "mediaUrl": "items/shop-1/item-123-uuid.jpg",
    "mediaType": "IMAGE",
    "fileName": "product.jpg",
    "lastUpdated": "2025-01-07T10:30:00",
    "shopId": 1
  },
  "item_124": {
    "itemId": 124,
    "mediaUrl": "items/shop-1/item-124-uuid.mp4",
    "mediaType": "VIDEO",
    "fileName": "demo.mp4",
    "lastUpdated": "2025-01-07T10:35:00",
    "shopId": 1
  }
}
```

## Configuration

### **Application Properties**
```properties
# Media Storage Configuration
media.storage.base-path=media-storage
```

### **Default Values**
- Base path: `media-storage` (relative to application root)
- JSON files: Pretty-printed for readability
- Timestamps: ISO 8601 format with LocalDateTime

## Integration with Other Services

### **ItemService Integration**
```java
@Service
public class ItemService {
    private final MediaStorageService mediaStorageService;
    
    public Item createItem(Item item) {
        Item savedItem = itemRepository.save(item);
        
        // Save media metadata
        if (savedItem.getMediaUrl() != null) {
            mediaStorageService.saveItemMedia(
                savedItem.getShop().getId(),
                savedItem.getId(),
                savedItem.getMediaUrl(),
                savedItem.getMediaType(),
                savedItem.getMediaFileName()
            );
        }
        
        return savedItem;
    }
}
```

### **FileStorageService Coordination**
- `FileStorageService`: Handles physical file operations
- `MediaStorageService`: Handles metadata and organization
- Both services work together for complete media management

## Error Handling

### **Exception Strategy**
- **IOException**: Wrapped in RuntimeException for service layer
- **JSON Parsing**: Graceful fallback to empty data
- **Missing Files**: Returns empty Optional/Map instead of exceptions
- **Directory Creation**: Automatic with proper error logging

### **Logging Levels**
- **DEBUG**: Detailed operation logging
- **INFO**: Important state changes
- **WARN**: Recoverable errors (e.g., corrupted JSON)
- **ERROR**: Critical failures requiring attention

## Performance Considerations

### **Optimizations**
- **Lazy Loading**: JSON files loaded only when needed
- **Caching**: Consider adding caching for frequently accessed shops
- **Batch Operations**: Single file write for multiple updates
- **Directory Structure**: Flat structure for fast access

### **Scalability**
- **Shop Isolation**: Each shop has separate metadata file
- **File Size**: JSON files remain small (metadata only)
- **Concurrent Access**: File-based storage handles concurrent reads well
- **Backup**: JSON files are easily backed up and restored

## Best Practices

### **Usage Patterns**
1. **Always save metadata** when storing physical files
2. **Check metadata first** before file operations
3. **Clean up metadata** when deleting items
4. **Use bulk operations** for shop-level operations
5. **Monitor health status** in production

### **Error Recovery**
1. **Corrupted JSON**: Service creates new empty file
2. **Missing Directories**: Automatically created on demand
3. **Orphaned Metadata**: Regular cleanup recommended
4. **Inconsistent State**: Metadata can be rebuilt from database

## Testing

### **Unit Tests**
- Complete test coverage in `MediaStorageServiceTest`
- Tests all CRUD operations
- Verifies shop isolation
- Checks error handling scenarios

### **Integration Testing**
```java
@Test
void testMediaStorageIntegration() {
    // Create item with media
    Item item = itemService.createItem(itemWithMedia);
    
    // Verify metadata was saved
    Optional<Map<String, Object>> metadata = 
        mediaStorageService.getItemMedia(shopId, item.getId());
    
    assertTrue(metadata.isPresent());
    assertEquals(item.getMediaUrl(), metadata.get().get("mediaUrl"));
}
```

This service provides a robust, scalable solution for managing media metadata in the Market Application while maintaining clear separation of concerns with the physical file storage system.

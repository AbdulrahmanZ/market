# Media API Quick Reference

## 🚀 Quick Start

### Get Shop Profile Image
```http
GET /api/files/shop/{shopId}/profile
```

### Get Item Media (Image/Video)
```http
GET /api/files/item/{itemId}/media
```

### Check What Media Exists
```http
GET /api/files/shop/{shopId}/profile/info
GET /api/files/item/{itemId}/media/info
```

## 📋 All Endpoints

| Endpoint | Method | Description | Returns |
|----------|--------|-------------|---------|
| `/api/files/shop/{shopId}/profile` | GET | Shop profile image | Binary image |
| `/api/files/shop/{shopId}/profile/info` | GET | Shop profile metadata | JSON |
| `/api/files/item/{itemId}/media` | GET | Item media file | Binary media |
| `/api/files/item/{itemId}/media/info` | GET | Item media metadata | JSON |
| `/api/files/shop/{shopId}/items/media/list` | GET | All shop media list | JSON |
| `/api/files/shop-profiles/{shopId}/{filename}` | GET | Profile by filename | Binary image |
| `/api/files/items/{shopId}/{filename}` | GET | Item media by filename | Binary media |
| `/api/files/health` | GET | Service health check | JSON |

## 🔧 Response Headers

### Images
- `Content-Type`: `image/jpeg`, `image/png`, `image/gif`, `image/webp`
- `Cache-Control`: `max-age=3600` (1 hour)
- `Content-Disposition`: `inline; filename="..."`

### Videos
- `Content-Type`: `video/mp4`, `video/avi`, `video/quicktime`, etc.
- `Cache-Control`: `max-age=86400` (24 hours)
- `Accept-Ranges`: `bytes` (enables seeking)
- `Content-Disposition`: `inline; filename="..."`

## 📱 Client Examples

### HTML/JavaScript
```html
<!-- Shop Profile -->
<img src="/api/files/shop/1/profile" alt="Shop Profile" />

<!-- Item Media -->
<img src="/api/files/item/5/media" alt="Item Image" />
<video src="/api/files/item/6/media" controls></video>
```

### JavaScript Fetch
```javascript
// Get shop profile
const response = await fetch('/api/files/shop/1/profile');
const blob = await response.blob();
const imageUrl = URL.createObjectURL(blob);

// Check if item has media first
const info = await fetch('/api/files/item/5/media/info').then(r => r.json());
if (info.hasMedia) {
  const mediaUrl = '/api/files/item/5/media';
  // Use mediaUrl in img or video tag
}
```

### Flutter/Dart
```dart
// In your widget
Image.network('http://localhost:8080/api/files/shop/1/profile')

// Or with error handling
FutureBuilder<Uint8List?>(
  future: http.get(Uri.parse('/api/files/item/5/media')).then((r) => r.bodyBytes),
  builder: (context, snapshot) {
    if (snapshot.hasData) return Image.memory(snapshot.data!);
    return CircularProgressIndicator();
  },
)
```

### cURL
```bash
# Download shop profile
curl -o shop_profile.jpg http://localhost:8080/api/files/shop/1/profile

# Download item media
curl -o item_media.mp4 http://localhost:8080/api/files/item/5/media

# Get media info
curl http://localhost:8080/api/files/item/5/media/info
```

## ⚠️ Error Handling

| Status Code | Meaning | Action |
|-------------|---------|--------|
| 200 | Success | Media found and returned |
| 404 | Not Found | Entity doesn't exist or has no media |
| 500 | Server Error | Check server logs |

### Best Practices
1. **Always check info endpoints first** to avoid unnecessary requests
2. **Handle 404 gracefully** with placeholder images
3. **Respect cache headers** for better performance
4. **Use appropriate HTML elements** (`<img>` for images, `<video>` for videos)

## 🎯 Common Use Cases

### Display Shop Profile in List
```javascript
// Get all shops, then load profiles
const shops = await fetch('/api/shops').then(r => r.json());
shops.content.forEach(shop => {
  const img = document.createElement('img');
  img.src = `/api/files/shop/${shop.id}/profile`;
  img.onerror = () => img.src = '/placeholder-shop.png'; // Fallback
  container.appendChild(img);
});
```

### Display Item Gallery
```javascript
// Get items for a shop, then load media
const items = await fetch('/api/items/shop/1').then(r => r.json());
items.forEach(async item => {
  const info = await fetch(`/api/files/item/${item.id}/media/info`).then(r => r.json());
  
  if (info.hasMedia) {
    const mediaElement = info.mediaType === 'VIDEO' 
      ? document.createElement('video')
      : document.createElement('img');
    
    mediaElement.src = `/api/files/item/${item.id}/media`;
    if (info.mediaType === 'VIDEO') mediaElement.controls = true;
    
    gallery.appendChild(mediaElement);
  }
});
```

### Bulk Media Loading
```javascript
// Get all media for a shop at once
const mediaList = await fetch('/api/files/shop/1/items/media/list').then(r => r.json());

console.log(`Found ${mediaList.itemsWithMedia} items with media`);
console.log(`${mediaList.imageCount} images, ${mediaList.videoCount} videos`);

// Load each media item
Object.values(mediaList.mediaList).forEach(media => {
  const element = media.mediaType === 'VIDEO' 
    ? document.createElement('video')
    : document.createElement('img');
  
  element.src = media.directAccessUrl;
  container.appendChild(element);
});
```

## 🔍 Debugging

### Check Service Health
```http
GET /api/files/health
```

### Test with Known IDs
```http
GET /api/files/shop/1/profile/info
GET /api/files/item/1/media/info
```

### Check File System
- Shop profiles: `uploads/shop-profiles/shop-{id}/`
- Item media: `uploads/items/shop-{shopId}/`

### Common Issues
1. **404 on existing entities**: Check if media was actually uploaded
2. **CORS errors**: Ensure proper CORS configuration for cross-origin requests
3. **Large video loading**: Videos support range requests for streaming
4. **Cache issues**: Check cache headers and browser cache

## 📊 Performance Tips

1. **Use info endpoints** to check existence before loading
2. **Implement lazy loading** for large galleries
3. **Cache media URLs** on client side
4. **Use appropriate image sizes** (consider adding thumbnail endpoints)
5. **Leverage browser caching** with proper cache headers

This quick reference should help you efficiently fetch and display all media in your Market Application! 🎉

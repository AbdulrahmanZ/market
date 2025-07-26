# Media API Guide - Fetching Images and Videos

## Overview
This guide explains how to fetch images and videos for shops and items in the Market Application. The system provides multiple ways to access media files with different levels of convenience and functionality.

## File Storage Structure
```
uploads/
├── shop-profiles/
│   └── shop-{shopId}/
│       └── profile-{uuid}.{ext}
└── items/
    └── shop-{shopId}/
        └── item-{itemId}-{uuid}.{ext}
```

## API Endpoints Summary

### 🏪 **Shop Profile Images**

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/files/shop/{shopId}/profile` | Get shop profile image by shop ID | No |
| GET | `/api/files/shop/{shopId}/profile/info` | Get shop profile image metadata | No |
| GET | `/api/files/shop-profiles/{shopId}/{filename}` | Get shop profile by filename | No |

### 📦 **Item Media (Images/Videos)**

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/files/item/{itemId}/media` | Get item media by item ID | No |
| GET | `/api/files/item/{itemId}/media/info` | Get item media metadata | No |
| GET | `/api/files/items/{shopId}/{filename}` | Get item media by filename | No |

### 📋 **Media Listing**

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/files/shop/{shopId}/items/media/list` | List all media for shop's items | No |
| GET | `/api/files/health` | Check file service health | No |

## Usage Examples

### **1. Fetch Shop Profile Image**

#### By Shop ID (Recommended)
```http
GET /api/files/shop/1/profile
```

**Response**: Binary image data with appropriate content-type headers

**Headers Returned**:
- `Content-Type`: `image/jpeg`, `image/png`, etc.
- `Content-Disposition`: `inline; filename="profile-uuid.jpg"`
- `Cache-Control`: `max-age=3600`

#### Get Shop Profile Info First
```http
GET /api/files/shop/1/profile/info
```

**Response**:
```json
{
  "shopId": 1,
  "shopName": "Electronics Store",
  "hasProfileImage": true,
  "profileImageUrl": "shop-profiles/shop-1/profile-uuid.jpg",
  "directAccessUrl": "/api/files/shop/1/profile",
  "fileExists": true,
  "contentType": "image/jpeg",
  "filename": "profile-uuid.jpg"
}
```

### **2. Fetch Item Media**

#### By Item ID (Recommended)
```http
GET /api/files/item/5/media
```

**Response**: Binary media data (image or video)

**Headers for Images**:
- `Content-Type`: `image/jpeg`, `image/png`, etc.
- `Cache-Control`: `max-age=3600`

**Headers for Videos**:
- `Content-Type`: `video/mp4`, `video/avi`, etc.
- `Cache-Control`: `max-age=86400`
- `Accept-Ranges`: `bytes` (enables video seeking)

#### Get Item Media Info First
```http
GET /api/files/item/5/media/info
```

**Response**:
```json
{
  "itemId": 5,
  "shopId": 1,
  "hasMedia": true,
  "mediaUrl": "items/shop-1/item-5-uuid.mp4",
  "mediaType": "VIDEO",
  "mediaFileName": "product-demo.mp4",
  "directAccessUrl": "/api/files/item/5/media",
  "fileExists": true,
  "contentType": "video/mp4"
}
```

### **3. List All Media for a Shop**

```http
GET /api/files/shop/1/items/media/list
```

**Response**:
```json
{
  "shopId": 1,
  "shopName": "Electronics Store",
  "totalItems": 10,
  "itemsWithMedia": 8,
  "imageCount": 6,
  "videoCount": 2,
  "mediaList": {
    "item_5": {
      "itemId": 5,
      "mediaUrl": "items/shop-1/item-5-uuid.mp4",
      "mediaType": "VIDEO",
      "mediaFileName": "demo.mp4",
      "directAccessUrl": "/api/files/item/5/media",
      "fileExists": true
    },
    "item_6": {
      "itemId": 6,
      "mediaUrl": "items/shop-1/item-6-uuid.jpg",
      "mediaType": "IMAGE",
      "mediaFileName": "product.jpg",
      "directAccessUrl": "/api/files/item/6/media",
      "fileExists": true
    }
  }
}
```

## Client Implementation Examples

### **JavaScript/Web**

```javascript
// Fetch shop profile image
async function getShopProfile(shopId) {
  try {
    const response = await fetch(`/api/files/shop/${shopId}/profile`);
    if (response.ok) {
      const blob = await response.blob();
      const imageUrl = URL.createObjectURL(blob);
      
      // Use the image URL in an img tag
      document.getElementById('shop-profile').src = imageUrl;
    }
  } catch (error) {
    console.error('Error fetching shop profile:', error);
  }
}

// Fetch item media with info
async function getItemMedia(itemId) {
  try {
    // First get media info
    const infoResponse = await fetch(`/api/files/item/${itemId}/media/info`);
    const info = await infoResponse.json();
    
    if (info.hasMedia) {
      // Then fetch the actual media
      const mediaResponse = await fetch(`/api/files/item/${itemId}/media`);
      const blob = await mediaResponse.blob();
      const mediaUrl = URL.createObjectURL(blob);
      
      if (info.mediaType === 'VIDEO') {
        // Handle video
        const video = document.getElementById('item-video');
        video.src = mediaUrl;
      } else {
        // Handle image
        const img = document.getElementById('item-image');
        img.src = mediaUrl;
      }
    }
  } catch (error) {
    console.error('Error fetching item media:', error);
  }
}

// List all shop media
async function listShopMedia(shopId) {
  try {
    const response = await fetch(`/api/files/shop/${shopId}/items/media/list`);
    const data = await response.json();
    
    console.log(`Shop has ${data.itemsWithMedia} items with media`);
    console.log(`Images: ${data.imageCount}, Videos: ${data.videoCount}`);
    
    // Process each media item
    Object.values(data.mediaList).forEach(media => {
      console.log(`Item ${media.itemId}: ${media.mediaType} - ${media.directAccessUrl}`);
    });
  } catch (error) {
    console.error('Error listing shop media:', error);
  }
}
```

### **Flutter/Dart**

```dart
import 'package:http/http.dart' as http;
import 'dart:typed_data';

class MediaService {
  final String baseUrl = 'http://localhost:8080';

  // Fetch shop profile image
  Future<Uint8List?> getShopProfile(int shopId) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/api/files/shop/$shopId/profile'),
      );
      
      if (response.statusCode == 200) {
        return response.bodyBytes;
      }
      return null;
    } catch (e) {
      print('Error fetching shop profile: $e');
      return null;
    }
  }

  // Fetch item media
  Future<Uint8List?> getItemMedia(int itemId) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/api/files/item/$itemId/media'),
      );
      
      if (response.statusCode == 200) {
        return response.bodyBytes;
      }
      return null;
    } catch (e) {
      print('Error fetching item media: $e');
      return null;
    }
  }

  // Get media info
  Future<Map<String, dynamic>?> getItemMediaInfo(int itemId) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/api/files/item/$itemId/media/info'),
      );
      
      if (response.statusCode == 200) {
        return json.decode(response.body);
      }
      return null;
    } catch (e) {
      print('Error fetching media info: $e');
      return null;
    }
  }
}

// Usage in Flutter Widget
class ItemMediaWidget extends StatelessWidget {
  final int itemId;
  
  const ItemMediaWidget({Key? key, required this.itemId}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<Uint8List?>(
      future: MediaService().getItemMedia(itemId),
      builder: (context, snapshot) {
        if (snapshot.hasData) {
          return Image.memory(snapshot.data!);
        } else if (snapshot.hasError) {
          return Icon(Icons.error);
        }
        return CircularProgressIndicator();
      },
    );
  }
}
```

### **Python**

```python
import requests
from typing import Optional, Dict, Any

class MediaClient:
    def __init__(self, base_url: str = "http://localhost:8080"):
        self.base_url = base_url
    
    def get_shop_profile(self, shop_id: int) -> Optional[bytes]:
        """Fetch shop profile image"""
        try:
            response = requests.get(f"{self.base_url}/api/files/shop/{shop_id}/profile")
            if response.status_code == 200:
                return response.content
            return None
        except Exception as e:
            print(f"Error fetching shop profile: {e}")
            return None
    
    def get_item_media(self, item_id: int) -> Optional[bytes]:
        """Fetch item media (image or video)"""
        try:
            response = requests.get(f"{self.base_url}/api/files/item/{item_id}/media")
            if response.status_code == 200:
                return response.content
            return None
        except Exception as e:
            print(f"Error fetching item media: {e}")
            return None
    
    def get_item_media_info(self, item_id: int) -> Optional[Dict[str, Any]]:
        """Get item media information"""
        try:
            response = requests.get(f"{self.base_url}/api/files/item/{item_id}/media/info")
            if response.status_code == 200:
                return response.json()
            return None
        except Exception as e:
            print(f"Error fetching media info: {e}")
            return None
    
    def list_shop_media(self, shop_id: int) -> Optional[Dict[str, Any]]:
        """List all media for a shop"""
        try:
            response = requests.get(f"{self.base_url}/api/files/shop/{shop_id}/items/media/list")
            if response.status_code == 200:
                return response.json()
            return None
        except Exception as e:
            print(f"Error listing shop media: {e}")
            return None

# Usage example
client = MediaClient()

# Get shop profile
profile_data = client.get_shop_profile(1)
if profile_data:
    with open("shop_profile.jpg", "wb") as f:
        f.write(profile_data)

# Get item media info first
media_info = client.get_item_media_info(5)
if media_info and media_info['hasMedia']:
    # Then get the actual media
    media_data = client.get_item_media(5)
    if media_data:
        extension = media_info['contentType'].split('/')[-1]
        filename = f"item_5_media.{extension}"
        with open(filename, "wb") as f:
            f.write(media_data)
```

## Error Handling

### **Common HTTP Status Codes**

- **200 OK**: Media found and returned
- **404 Not Found**: Media file doesn't exist or entity not found
- **500 Internal Server Error**: Server error (check logs)

### **Best Practices**

1. **Check Info First**: Use info endpoints to verify media exists before fetching
2. **Handle Missing Media**: Always check if entities have media before attempting to fetch
3. **Caching**: Implement client-side caching for frequently accessed media
4. **Error Handling**: Provide fallback images/placeholders for missing media
5. **Content-Type**: Check response content-type to handle images vs videos appropriately

## Performance Considerations

1. **Caching Headers**: The API sets appropriate cache headers - respect them
2. **Range Requests**: Videos support range requests for efficient streaming
3. **Batch Operations**: Use listing endpoints to get multiple media URLs at once
4. **Lazy Loading**: Load media only when needed (e.g., when scrolling into view)

This comprehensive media API provides flexible and efficient ways to fetch all types of media files in your Market Application.

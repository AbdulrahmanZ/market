package com.market.service;

import com.market.model.Item;
import com.market.model.MediaType;
import com.market.model.Shop;
import com.market.repository.ItemRepository;
import com.market.exception.ItemLimitExceededException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final MediaStorageService mediaStorageService;
    private final FileStorageService fileStorageService;
    private final ShopService shopService;

    public ItemService(ItemRepository itemRepository, MediaStorageService mediaStorageService, FileStorageService fileStorageService, ShopService shopService) {
        this.itemRepository = itemRepository;
        this.mediaStorageService = mediaStorageService;
        this.fileStorageService = fileStorageService;
        this.shopService = shopService;
    }
    
    public Item createItem(Item item) {
        // Validate item limit before creating
        validateItemLimit(item.getShop().getId());

        Item savedItem = itemRepository.save(item);

        // Save media URL to file storage if media exists
        if (savedItem.getMediaUrl() != null && savedItem.getShop() != null) {
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
    
    public Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
    }
    
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public Page<Item> getAllItems(Pageable pageable) {
        return itemRepository.findAll(pageable);
    }

    public List<Item> getItemsByShop(Long shopId) {
        return itemRepository.findByShopId(shopId);
    }

    public Page<Item> getItemsByShop(Long shopId, Pageable pageable) {
        return itemRepository.findByShopId(shopId, pageable);
    }

    public List<Item> getItemsByMediaType(MediaType mediaType) {
        return itemRepository.findByMediaType(mediaType);
    }

    public Page<Item> getItemsByMediaType(MediaType mediaType, Pageable pageable) {
        return itemRepository.findByMediaType(mediaType, pageable);
    }

    public List<Item> getItemsByShopAndMediaType(Long shopId, MediaType mediaType) {
        return itemRepository.findByShopIdAndMediaType(shopId, mediaType);
    }

    public Page<Item> getItemsByShopAndMediaType(Long shopId, MediaType mediaType, Pageable pageable) {
        return itemRepository.findByShopIdAndMediaType(shopId, mediaType, pageable);
    }

    public List<Item> searchItemsByDescription(String description) {
        return itemRepository.findByDescriptionContainingIgnoreCase(description);
    }

    public Page<Item> searchItemsByDescription(String description, Pageable pageable) {
        return itemRepository.findByDescriptionContainingIgnoreCase(description, pageable);
    }

    public List<Item> getItemsByPriceRange(Double minPrice, Double maxPrice) {
        return itemRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public Page<Item> getItemsByPriceRange(Double minPrice, Double maxPrice, Pageable pageable) {
        return itemRepository.findByPriceBetween(minPrice, maxPrice, pageable);
    }
    
    public Item updateItem(Long id, Item itemDetails) {
        Item item = getItemById(id);

        item.setDescription(itemDetails.getDescription());
        item.setName(itemDetails.getName());
        item.setPrice(itemDetails.getPrice());
        item.setMediaUrl(itemDetails.getMediaUrl());
        item.setMediaType(itemDetails.getMediaType());
        item.setMediaFileName(itemDetails.getMediaFileName());

        Item updatedItem = itemRepository.save(item);

        // Update media URL in file storage if media exists
        if (updatedItem.getMediaUrl() != null && updatedItem.getShop() != null) {
            mediaStorageService.saveItemMedia(
                updatedItem.getShop().getId(),
                updatedItem.getId(),
                updatedItem.getMediaUrl(),
                updatedItem.getMediaType(),
                updatedItem.getMediaFileName()
            );
        } else if (updatedItem.getShop() != null) {
            // If media was removed, delete from file storage
            mediaStorageService.deleteItemMedia(updatedItem.getShop().getId(), updatedItem.getId());
        }

        return updatedItem;
    }
    
    public void deleteItem(Long id) {
        Item item = getItemById(id);

        // Delete media from file storage before deleting item
        if (item.getShop() != null) {
            mediaStorageService.deleteItemMedia(item.getShop().getId(), item.getId());
        }

        // Delete actual media file if exists
        if (item.getMediaUrl() != null && !item.getMediaUrl().isEmpty()) {
            fileStorageService.deleteFile(item.getMediaUrl());
        }

        itemRepository.deleteById(id);
    }

    // Media management methods

    public Map<String, Object> getShopMediaUrls(Long shopId) {
        return mediaStorageService.getAllShopMedia(shopId);
    }

    public Map<String, Object> getShopMediaStats(Long shopId) {
        return mediaStorageService.getShopMediaStats(shopId);
    }

    public void deleteAllShopMedia(Long shopId) {
        mediaStorageService.deleteShopMedia(shopId);
    }

    public com.market.model.Shop getShopById(Long shopId) {
        return shopService.getShopById(shopId);
    }

    private void validateItemLimit(Long shopId) {
        com.market.model.Shop shop = shopService.getShopById(shopId);

        if (shop.getItemLimit() != null && shop.getItemLimit() > 0) {
            List<Item> existingItems = getItemsByShop(shopId);
            int currentItemCount = existingItems.size();

            if (currentItemCount >= shop.getItemLimit()) {
                throw new ItemLimitExceededException(
                    shop.getId(),
                    shop.getName(),
                    shop.getItemLimit(),
                    currentItemCount
                );
            }
        }
    }

    public int getCurrentItemCount(Long shopId) {
        return getItemsByShop(shopId).size();
    }

    public int getRemainingItemSlots(Long shopId) {
        Shop shop = shopService.getShopByIdWithLock(shopId);
        if (shop.getItemLimit() == null || shop.getItemLimit() <= 0) {
            return Integer.MAX_VALUE; // Unlimited
        }

        int currentCount = getCurrentItemCount(shopId);
        return Math.max(0, shop.getItemLimit() - currentCount);
    }

    public boolean canAddMoreItems(Long shopId) {
        return getRemainingItemSlots(shopId) > 0;
    }

    public Page<Item> searchItemsAdvanced(
            String name,
            String description,
            Double minPrice,
            Double maxPrice,
            Long categoryId,
            Long townId,
            Pageable pageable
    ) {
        return itemRepository.searchItemsAdvanced(name, description, minPrice, maxPrice, categoryId, townId, pageable);
    }
}

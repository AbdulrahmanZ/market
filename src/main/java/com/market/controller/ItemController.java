package com.market.controller;

import com.market.dto.ItemRequest;
import com.market.model.*;
import com.market.service.ItemService;
import com.market.service.FileStorageService;
import com.market.service.AuthenticationService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/items")
public class ItemController {

    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);

    @Autowired
    ItemService itemService;

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    AuthenticationService authenticationService;


    @PostMapping()
    @Transactional
    public ResponseEntity<Item> createItem(
            @Valid @RequestPart("item") ItemRequest itemRequest,
            @RequestPart(value = "mediaFile", required = false) MultipartFile file) {
        try {
            // Require authentication
            authenticationService.requireAuthentication();

            // Check if user owns the shop
            Shop shop = itemService.getShopById(itemRequest.getShopId());
            authenticationService.requireOwnership(shop.getOwner().getId());

            // Check if shop can accept more items
            if (!itemService.canAddMoreItems(itemRequest.getShopId())) {
                throw new RuntimeException(String.format("Cannot add item. Shop '%s' has reached its item limit of %d items",
                        shop.getName(), shop.getItemLimit()));
            }

            Item item = new Item();
            item.setDescription(itemRequest.getDescription());
            item.setPrice(itemRequest.getPrice());

            // Set shop relationship
            item.setShop(shop);

            // Create item first to get ID
            Item createdItem = itemService.createItem(item);

            // Handle media file
            if (file != null && !file.isEmpty()) {
                String mediaPath = fileStorageService.storeItemMedia(file, itemRequest.getShopId(), createdItem.getId());
                MediaType mediaType = fileStorageService.determineMediaType(file);
                String originalFilename = file.getOriginalFilename();

                createdItem.setMediaUrl(mediaPath);
                createdItem.setMediaType(mediaType);
                createdItem.setMediaFileName(originalFilename);

                // Update item with media information
                createdItem = itemService.updateItem(createdItem.getId(), createdItem);
            }

            return ResponseEntity.ok(createdItem);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create item with media: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItem(@PathVariable Long id) {
        Item item = itemService.getItemById(id);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<?> getAllItems(Pageable pageable) {
        return ResponseEntity.ok(itemService.getAllItems(pageable));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Item> updateItem(
            @PathVariable Long id,
            @Valid @RequestPart(value = "item") ItemRequest itemRequest,
            @RequestPart(value = "mediaFile", required = false) MultipartFile file) {

        logger.info("Updating item with ID: {}", id);
        logger.debug("Item request: {}", itemRequest != null ? "present" : "null");
        logger.debug("Media file: {}", file != null ? "present (" + file.getOriginalFilename() + ")" : "null");

        try {
            // Require authentication
            authenticationService.requireAuthentication();

            // Check if user owns the shop that owns this item
            Item existingItem = itemService.getItemById(id);
            authenticationService.requireOwnership(existingItem.getShop().getOwner().getId());

            if (itemRequest != null) {
                // Update item details
                existingItem.setDescription(StringUtils.isEmpty(itemRequest.getDescription()) ? existingItem.getDescription() : itemRequest.getDescription());
                existingItem.setPrice(itemRequest.getPrice() == null ? existingItem.getPrice() : itemRequest.getPrice());

                // Note: We don't allow changing the shop ID for existing items for data integrity
                // If shop change is needed, it should be a separate business operation
            }

            // Handle media file update
            if (file != null && !file.isEmpty()) {
                // Delete old media file if exists
                if (existingItem.getMediaUrl() != null && !existingItem.getMediaUrl().isEmpty()) {
                    fileStorageService.deleteFile(existingItem.getMediaUrl());
                }

                // Store new media file
                String mediaPath = fileStorageService.storeItemMedia(file, existingItem.getShop().getId(), existingItem.getId());
                MediaType mediaType = fileStorageService.determineMediaType(file);
                String originalFilename = file.getOriginalFilename();

                existingItem.setMediaUrl(mediaPath);
                existingItem.setMediaType(mediaType);
                existingItem.setMediaFileName(originalFilename);
            }

            Item updatedItem = itemService.updateItem(id, existingItem);
            logger.info("Successfully updated item with ID: {}", id);
            return ResponseEntity.ok(updatedItem);
        } catch (IOException e) {
            logger.error("Error uploading media file for item ID: {}", id, e);
            throw new RuntimeException("Failed to upload media file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating item with ID: {}", id, e);
            throw new RuntimeException("Failed to update item: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        // Require authentication
        authenticationService.requireAuthentication();

        // Check if user owns the shop that owns this item
        Item item = itemService.getItemById(id);
        authenticationService.requireOwnership(item.getShop().getOwner().getId());

        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    /*****************************************************************************/

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<Page<Item>> getItemsByShop(
            @PathVariable Long shopId, Pageable pageable) {
        Page<Item> items = itemService.getItemsByShop(shopId, pageable);
        return ResponseEntity.ok(items);
    }



    @GetMapping("/shop/{shopId}/limit-info")
    public ResponseEntity<Map<String, Object>> getShopItemLimitInfo(@PathVariable Long shopId) {
        com.market.model.Shop shop = itemService.getShopById(shopId);
        int currentCount = itemService.getCurrentItemCount(shopId);
        int remainingSlots = itemService.getRemainingItemSlots(shopId);
        boolean canAddMore = itemService.canAddMoreItems(shopId);

        Map<String, Object> limitInfo = new HashMap<>();
        limitInfo.put("shopId", shopId);
        limitInfo.put("shopName", shop.getName());
        limitInfo.put("itemLimit", shop.getItemLimit());
        limitInfo.put("currentItemCount", currentCount);
        limitInfo.put("remainingSlots", remainingSlots == Integer.MAX_VALUE ? "unlimited" : remainingSlots);
        limitInfo.put("canAddMoreItems", canAddMore);

        return ResponseEntity.ok(limitInfo);
    }

    @GetMapping("/shop/{shopId}/can-add-item")
    public ResponseEntity<Map<String, Object>> canAddItem(@PathVariable Long shopId) {
        boolean canAdd = itemService.canAddMoreItems(shopId);
        int remaining = itemService.getRemainingItemSlots(shopId);

        Map<String, Object> response = new HashMap<>();
        response.put("canAddItem", canAdd);
        response.put("remainingSlots", remaining == Integer.MAX_VALUE ? "unlimited" : remaining);

        if (!canAdd) {
            com.market.model.Shop shop = itemService.getShopById(shopId);
            response.put("message", String.format("Shop has reached its item limit of %d items", shop.getItemLimit()));
        }

        return ResponseEntity.ok(response);
    }


}

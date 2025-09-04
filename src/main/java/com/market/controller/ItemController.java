package com.market.controller;

import com.market.dto.ItemRequest;
import com.market.model.*;
import com.market.service.ItemService;
import com.market.service.AuthenticationService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/items")
public class ItemController {

    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);
    @Autowired
    ItemService itemService;
    @Autowired
    AuthenticationService authenticationService;


    @PostMapping()
    @Transactional
    public ResponseEntity<Item> createItem(@Valid @RequestBody ItemRequest itemRequest) {
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
            item.setName(itemRequest.getName());
            item.setPrice(itemRequest.getPrice());
            item.setImageKey(itemRequest.getImageKey()); // Use imageKey from request
            item.setCurrencyType(itemRequest.getCurrencyType());

            // Set shop relationship
            item.setShop(shop);

            // Create item
            Item createdItem = itemService.createItem(item);

            return ResponseEntity.ok(createdItem);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create item: " + e.getMessage());
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
            @Valid @RequestBody ItemRequest itemRequest) {

        logger.info("Updating item with ID: {}", id);
        logger.debug("Item request: {}", itemRequest != null ? "present" : "null");

        try {
            // Require authentication
            authenticationService.requireAuthentication();

            // Check if user owns the shop that owns this item
            Item existingItem = itemService.getItemById(id);
            authenticationService.requireOwnership(existingItem.getShop().getOwner().getId());

            if (itemRequest != null) {
                // Update item details
                existingItem.setDescription(itemRequest.getDescription() != null ? existingItem.getDescription() : itemRequest.getDescription());
                existingItem.setName(itemRequest.getName() != null ? existingItem.getName() : itemRequest.getName());
                existingItem.setPrice(itemRequest.getPrice() == null ? existingItem.getPrice() : itemRequest.getPrice());
                existingItem.setCurrencyType(itemRequest.getCurrencyType() != null ? existingItem.getCurrencyType() : itemRequest.getCurrencyType());

                // Handle imageKey update (similar to ShopController)
                if (itemRequest.getImageKey() != null && !itemRequest.getImageKey().equals(existingItem.getImageKey())) {
                    existingItem.setImageKey(itemRequest.getImageKey());
                }

                // Note: We don't allow changing the shop ID for existing items for data integrity
                // If shop change is needed, it should be a separate business operation
            }

            Item updatedItem = itemService.updateItem(id, existingItem);
            logger.info("Successfully updated item with ID: {}", id);
            return ResponseEntity.ok(updatedItem);
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

}

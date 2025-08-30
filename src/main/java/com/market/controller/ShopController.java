package com.market.controller;

import com.market.model.Shop;
import com.market.model.Category;
import com.market.model.Town;
import com.market.model.User;
import com.market.dto.ShopRequest;
import com.market.repository.ShopRepository;
import com.market.service.ShopService;
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

@RestController
@RequestMapping("/shops")
public class ShopController {

    private static final Logger logger = LoggerFactory.getLogger(ShopController.class);

    @Autowired
    ShopService shopService;

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    ShopRepository shopRepository;

    @PostMapping()
    @Transactional
    public ResponseEntity<Shop> createShop(@Valid @RequestBody ShopRequest shopRequest) {
        try {
            if (!authenticationService.getCurrentUser().getAdmin()) {
                // Require authentication
                authenticationService.requireAuthentication();
                // Ensure user can only create shops for themselves
                authenticationService.requireOwnership(shopRequest.getOwnerId());
            }
            Shop createdShop = shopService.createShop(createShopFromRequest(shopRequest));
            return ResponseEntity.ok(createdShop);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static Shop createShopFromRequest(ShopRequest shopRequest) {
        Category category = new Category();
        category.setId(shopRequest.getCategoryId());

        Town town = new Town();
        town.setId(shopRequest.getTownId());

        User owner = new User();
        owner.setId(shopRequest.getOwnerId());

        Shop shop = new Shop(
                shopRequest.getName(),
                shopRequest.getDescription(),
                shopRequest.getAddress(),
                shopRequest.getPhone(),
                shopRequest.getItemLimit(),
                shopRequest.getImageKey(), // Use imageKey from request
                category,
                town,
                owner);

        // Set isActive if provided, otherwise default to true
        if (shopRequest.getIsActive() != null) {
            shop.setActive(shopRequest.getIsActive());
        } else {
            shop.setActive(true); // Default to active
        }

        return shop;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Shop> getShop(@PathVariable Long id) {
        Shop shop = shopService.getShopById(id);
        return ResponseEntity.ok(shop);
    }

    @GetMapping
    public ResponseEntity<Page<Shop>> getAllShops(Pageable pageable) {
        return ResponseEntity.ok(shopService.getAllShops(pageable));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Shop> updateShop(
            @PathVariable Long id,
            @Valid @RequestBody ShopRequest shopRequest) {

        logger.info("Updating shop with ID: {}", id);
        logger.debug("Shop request: {}", shopRequest != null ? "present" : "null");

        try {
            Shop existingShop = shopService.getShopById(id);
            if (!authenticationService.getCurrentUser().getAdmin()) {
                // Require authentication
                authenticationService.requireAuthentication();
                // Check if user owns the shop
                authenticationService.requireOwnership(existingShop.getOwner().getId());
            }
            if (shopRequest != null) {
                // Update fields only if provided (preserve existing values)
                if (shopRequest.getName() != null) {
                    existingShop.setName(shopRequest.getName());
                }
                if (shopRequest.getDescription() != null) {
                    existingShop.setDescription(shopRequest.getDescription());
                }
                if (shopRequest.getAddress() != null) {
                    existingShop.setAddress(shopRequest.getAddress());
                }
                if (shopRequest.getPhone() != null) {
                    existingShop.setPhone(shopRequest.getPhone());
                }
                if (shopRequest.getItemLimit() != null) {
                    existingShop.setItemLimit(shopRequest.getItemLimit());
                }

                // Update relationships only if provided
                if (shopRequest.getCategoryId() != null) {
                    Category category = new Category();
                    category.setId(shopRequest.getCategoryId());
                    existingShop.setCategory(category);
                }

                if (shopRequest.getTownId() != null) {
                    Town town = new Town();
                    town.setId(shopRequest.getTownId());
                    existingShop.setTown(town);
                }

                // Handle isActive field
                if (shopRequest.getIsActive() != null) {
                    existingShop.setActive(shopRequest.getIsActive());
                }

                // Handle image key update with cleanup
                if (shopRequest.getImageKey() != null && !shopRequest.getImageKey().equals(existingShop.getImageKey())) {
                    // Delete old profile image if exists and different from new one
                    if (existingShop.getImageKey() != null && !existingShop.getImageKey().isEmpty()) {
                        fileStorageService.deleteFile(existingShop.getImageKey());
                    }
                    existingShop.setImageKey(shopRequest.getImageKey());
                }
            }

            Shop updatedShop = shopService.updateShop(id, existingShop);
            logger.info("Successfully updated shop with ID: {}", id);
            return ResponseEntity.ok(updatedShop);
        } catch (Exception e) {
            logger.error("Error updating shop with ID: {}", id, e);
            throw new RuntimeException("Failed to update shop: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteShop(@PathVariable Long id) {
        Shop shop = shopService.getShopById(id);
        if (!authenticationService.getCurrentUser().getAdmin()) {
            // Require authentication
            authenticationService.requireAuthentication();
            // Check if user owns the shop
            authenticationService.requireOwnership(shop.getOwner().getId());
        }

        shopService.deleteShop(id);
        return ResponseEntity.noContent().build();
    }


    /****************************************************************************/

    @GetMapping("/{shopId}/is-owner/{userId}")
    public ResponseEntity<Boolean> isShopOwner(@PathVariable Long shopId,
                                               @PathVariable Long userId) {
        boolean isOwner = shopService.isShopOwner(shopId, userId);
        return ResponseEntity.ok(isOwner);
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> getShopCountByUser(@PathVariable Long userId) {
        long shopCount = shopService.getShopCountByUser(userId);
        return ResponseEntity.ok(shopCount);
    }

}

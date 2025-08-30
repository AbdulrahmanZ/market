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
import com.market.service.CategoryService;
import com.market.service.TownService;
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

    @Autowired
    CategoryService categoryService;

    @Autowired
    TownService townService;

    @PostMapping()
    @Transactional
    public ResponseEntity<Shop> createShop(
            @Valid @RequestPart("shop") ShopRequest shopRequest,
            @RequestPart(value = "profileImage", required = false) MultipartFile file) {
        try {
            if (!authenticationService.getCurrentUser().getAdmin()) {
                // Require authentication
                authenticationService.requireAuthentication();
                // Ensure user can only create shops for themselves
                authenticationService.requireOwnership(shopRequest.getOwnerId());
            }

            Category category = categoryService.getCategoryByName(shopRequest.getCategoryName());
            Town town = townService.getTownByName(shopRequest.getTownName());

            User owner = new User();
            owner.setId(shopRequest.getOwnerId());

            Shop shop = new Shop(
                    shopRequest.getName(),
                    shopRequest.getDescription(),
                    shopRequest.getAddress(),
                    shopRequest.getPhone(),
                    shopRequest.getItemLimit(),
                    null,
                    category,
                    town,
                    owner);

            // Create shop first to get ID
            Shop createdShop = shopService.createShop(shop);

            // Store profile image
            if (file != null) {
                String profileImagePath = fileStorageService.storeShopProfileImage(file, createdShop.getId());
                createdShop.setImageKey(profileImagePath);
                createdShop = shopRepository.save(createdShop);
            }

            return ResponseEntity.ok(createdShop);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create shop with image: " + e.getMessage());
        }
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
            @Valid @RequestPart(value = "shop") ShopRequest shopRequest,
            @RequestPart(value = "profileImage", required = false) MultipartFile file) {

        logger.info("Updating shop with ID: {}", id);
        logger.debug("Shop request: {}", shopRequest != null ? "present" : "null");
        logger.debug("Profile image: {}", file != null ? "present (" + file.getOriginalFilename() + ")" : "null");

        try {
            Shop existingShop = shopService.getShopById(id);
            if (!authenticationService.getCurrentUser().getAdmin()) {
                // Require authentication
                authenticationService.requireAuthentication();
                // Check if user owns the shop
                authenticationService.requireOwnership(existingShop.getOwner().getId());
            }

            if (shopRequest != null) {

                existingShop.setName(StringUtils.isEmpty(shopRequest.getName()) ? existingShop.getName() : shopRequest.getName());
                existingShop.setDescription(StringUtils.isEmpty(shopRequest.getDescription()) ? existingShop.getDescription() : shopRequest.getDescription());
                existingShop.setAddress(StringUtils.isEmpty(shopRequest.getAddress()) ? existingShop.getAddress() : shopRequest.getAddress());
                existingShop.setPhone(StringUtils.isEmpty(shopRequest.getPhone()) ? existingShop.getPhone() : shopRequest.getPhone());
                existingShop.setItemLimit(shopRequest.getItemLimit() == null ? existingShop.getItemLimit() : shopRequest.getItemLimit());

                // Set relationships using names
                if (shopRequest.getCategoryName() != null && !shopRequest.getCategoryName().isEmpty()) {
                    Category category = categoryService.getCategoryByName(shopRequest.getCategoryName());
                    existingShop.setCategory(category);
                }

                if (shopRequest.getTownName() != null && !shopRequest.getTownName().isEmpty()) {
                    Town town = townService.getTownByName(shopRequest.getTownName());
                    existingShop.setTown(town);
                }
            }

            if (file != null) {
                // Delete old profile image if exists
                if (existingShop.getImageKey() != null && !existingShop.getImageKey().isEmpty()) {
                    fileStorageService.deleteFile(existingShop.getImageKey());
                }

                // Store new profile image
                String profileImagePath = null;
                try {
                    profileImagePath = fileStorageService.storeShopProfileImage(file, existingShop.getId());
                } catch (IOException e) {
                    throw new RuntimeException("Failed to upload profile image: " + e.getMessage());
                }
                existingShop.setImageKey(profileImagePath);

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

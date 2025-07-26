package com.market.service;

import com.market.model.Shop;
import com.market.model.Category;
import com.market.model.Town;
import com.market.model.User;
import com.market.repository.ShopRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ShopService {
    
    private final ShopRepository shopRepository;
    private final CategoryService categoryService;
    private final TownService townService;
    private final UserService userService;
    private final FileStorageService fileStorageService;

    public ShopService(ShopRepository shopRepository, CategoryService categoryService,
                      TownService townService, UserService userService, FileStorageService fileStorageService) {
        this.shopRepository = shopRepository;
        this.categoryService = categoryService;
        this.townService = townService;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }
    
    public Shop createShop(Shop shop) {
        // Validate that owner, category, and town exist
        if (shop.getOwner() != null && shop.getOwner().getId() != null) {
            User owner = userService.getUserById(shop.getOwner().getId());
            shop.setOwner(owner);
        }
        
        if (shop.getCategory() != null && shop.getCategory().getId() != null) {
            Category category = categoryService.getCategoryById(shop.getCategory().getId());
            shop.setCategory(category);
        }
        
        if (shop.getTown() != null && shop.getTown().getId() != null) {
            Town town = townService.getTownById(shop.getTown().getId());
            shop.setTown(town);
        }
        
        // Check if shop name already exists for this owner
        if (shopRepository.existsByNameAndOwnerId(shop.getName(), shop.getOwner().getId())) {
            throw new RuntimeException("Shop name already exists for this owner");
        }
        
        return shopRepository.save(shop);
    }
    
    public Shop getShopById(Long id) {
        return shopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
    }

    public Shop getShopByIdWithLock(Long id) {
        return shopRepository.findByIdWithLock(id)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
    }

    public Page<Shop> getAllShops(Pageable pageable) {
        return shopRepository.findAll(pageable);
    }
    
    public List<Shop> getShopsByOwner(Long ownerId) {
        return shopRepository.findByOwnerId(ownerId);
    }

    public Page<Shop> getShopsByOwner(Long ownerId, Pageable pageable) {
        return shopRepository.findByOwnerId(ownerId, pageable);
    }

    public List<Shop> getShopsByTown(Long townId) {
        return shopRepository.findByTownId(townId);
    }

    public Page<Shop> getShopsByTown(Long townId, Pageable pageable) {
        return shopRepository.findByTownId(townId, pageable);
    }

    public List<Shop> getShopsByCategory(Long categoryId) {
        return shopRepository.findByCategoryId(categoryId);
    }

    public Page<Shop> getShopsByCategory(Long categoryId, Pageable pageable) {
        return shopRepository.findByCategoryId(categoryId, pageable);
    }
    
    public Shop updateShop(Long id, Shop shopDetails) {
        Shop shop = getShopById(id);
        
        // Check if name is being changed and if new name already exists for this owner
        if (!shop.getName().equals(shopDetails.getName()) && 
            shopRepository.existsByNameAndOwnerId(shopDetails.getName(), shop.getOwner().getId())) {
            throw new RuntimeException("Shop name already exists for this owner");
        }
        
        shop.setName(shopDetails.getName());
        shop.setDescription(shopDetails.getDescription());
        shop.setAddress(shopDetails.getAddress());
        shop.setPhone(shopDetails.getPhone());
        shop.setItemLimit(shopDetails.getItemLimit());
        shop.setProfileImageUrl(shopDetails.getProfileImageUrl());
        
        // Update category if provided
        if (shopDetails.getCategory() != null && shopDetails.getCategory().getId() != null) {
            Category category = categoryService.getCategoryById(shopDetails.getCategory().getId());
            shop.setCategory(category);
        }
        
        // Update town if provided
        if (shopDetails.getTown() != null && shopDetails.getTown().getId() != null) {
            Town town = townService.getTownById(shopDetails.getTown().getId());
            shop.setTown(town);
        }
        
        return shopRepository.save(shop);
    }
    
    public void deleteShop(Long id) {
        Shop shop = getShopById(id);

        // Delete profile image file if exists
        if (shop.getProfileImageUrl() != null && !shop.getProfileImageUrl().isEmpty()) {
            fileStorageService.deleteFile(shop.getProfileImageUrl());
        }

        shopRepository.delete(shop);
    }
    
    public boolean isShopOwner(Long shopId, Long userId) {
        Shop shop = getShopById(shopId);
        return shop.getOwner().getId().equals(userId);
    }
}

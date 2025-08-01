package com.market.controller;

import com.market.model.*;
import com.market.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
public class SearchController {
    @Autowired
    UserService userService;
    @Autowired
    TownService townService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    ShopService shopService;
    @Autowired
    ItemService itemService;

    /**
     * USERS
     **/
    @GetMapping("/users")
    public ResponseEntity<Page<User>> searchUsers(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String username,
            Pageable pageable) {
        Page<User> users = userService.searchUsers(phone, username, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * TOWNS
     **/
    @GetMapping("/towns")
    public ResponseEntity<Page<Town>> searchTowns(@RequestParam String name, Pageable pageable) {
        return ResponseEntity.ok(townService.getTownByContainingName(name, pageable));
    }

    /**
     * CATEGORIES
     **/
    @GetMapping("/categories")
    public ResponseEntity<Page<Category>> searchCategories(@RequestParam String name, Pageable pageable) {
        return ResponseEntity.ok(categoryService.getCategoryByContainingName(name, pageable));
    }

    /**
     * SHOPS
     **/
    @GetMapping("/shop-by-owner/{ownerId}")
    public ResponseEntity<Page<Shop>> getShopsByOwner(
            @PathVariable Long ownerId,
            Pageable pageable) {
        Page<Shop> shops = shopService.getShopsByOwner(ownerId, pageable);
        return ResponseEntity.ok(shops);
    }

    @GetMapping("/shop-by-town/{townId}")
    public ResponseEntity<Page<Shop>> getShopsByTown(
            @PathVariable Long townId,
            Pageable pageable) {
        Page<Shop> shops = shopService.getShopsByTown(townId, pageable);
        return ResponseEntity.ok(shops);
    }

    @GetMapping("/shop-by-category/{categoryId}")
    public ResponseEntity<Page<Shop>> getShopsByCategory(
            @PathVariable Long categoryId,
            Pageable pageable) {
        Page<Shop> shops = shopService.getShopsByCategory(categoryId, pageable);
        return ResponseEntity.ok(shops);
    }

    /**
     * ITEMS
     **/

    @GetMapping("/items-by-description")
    public ResponseEntity<Page<Item>> searchItems(
            @RequestParam String description, Pageable pageable) {
        Page<Item> items = itemService.searchItemsByDescription(description, pageable);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/items-by-price-range")
    public ResponseEntity<Page<Item>> getItemsByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice,
            Pageable pageable) {

        Page<Item> items = itemService.getItemsByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(items);
    }
}

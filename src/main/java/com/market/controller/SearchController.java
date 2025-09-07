package com.market.controller;

import com.market.model.*;
import com.market.projection.ItemProjection;
import com.market.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/active-shop-by-category-and-town/{townId}/{categoryId}")
    public ResponseEntity<Page<Shop>> getActiveShopsByCategoryAndTown(
            @PathVariable Long townId,
            @PathVariable Long categoryId,
            Pageable pageable) {
        Page<Shop> shops = shopService.getActiveShopsByCategoryAndTown(townId, categoryId, pageable);
        return ResponseEntity.ok(shops);
    }

    /*************************************************/

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

    @GetMapping("/items-by-shop/{shopId}")
    public ResponseEntity<?> getAvailableItemsByShop(
            @PathVariable Long shopId,
            Pageable pageable) {
        Page<ItemProjection> items = itemService.getAvailableItemsByShop(shopId, pageable);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/items-advanced")
    public ResponseEntity<?> searchItemsAdvanced(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long townId,
            Pageable pageable
    ) {
        Page<ItemProjection> items = itemService.searchItemsAdvanced(name, description, minPrice, maxPrice, categoryId, townId, pageable);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/items-by-ids-list")
    public ResponseEntity<?> getItemsByIdsList(@RequestParam String list) {
        if (list.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        System.out.println(list);
        List<ItemProjection> items = itemService.getItemsByIdsList(
                Arrays.stream(list.split(",")).map(String::trim).collect(Collectors.toList())
        );
        return ResponseEntity.ok(items);
    }

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

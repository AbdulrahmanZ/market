package com.market.repository;

import com.market.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query(
            "SELECT i FROM Item i WHERE i.shop.id = :shopId AND i.deleted = false"
    )
    List<Item> findByShopId(Long shopId);

    List<Item> findByDescriptionContainingIgnoreCase(String name);

    List<Item> findByPriceBetween(Double minPrice, Double maxPrice);

    // Pageable versions
    Page<Item> findByShopIdAndDeletedFalse(Long shopId, Pageable pageable);

    Page<Item> findByDescriptionContainingIgnoreCase(String name, Pageable pageable);

    Page<Item> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

    Page<Item> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT i FROM Item i  " +
            "Join Shop s ON s.id = i.shop.id WHERE s.isActive = true AND s.deleted = false AND i.deleted = false AND " +
            "(:name IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:description IS NULL OR LOWER(i.description) LIKE LOWER(CONCAT('%', :description, '%'))) AND " +
            "(:categoryId IS NULL OR i.shop.category.id = :categoryId) AND " +
            "(:townId IS NULL OR i.shop.town.id = :townId) AND " +
            "(:minPrice IS NULL OR i.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR i.price <= :maxPrice)")
    Page<Item> searchItemsAdvanced(
            @Param("name") String name,
            @Param("description") String description,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("categoryId") Long categoryId,
            @Param("townId") Long townId,
            Pageable pageable
    );

    @Modifying
    @Query("update Item i SET i.deleted = true WHERE i.id = :id")
    void softDeleteById(Long id);
}

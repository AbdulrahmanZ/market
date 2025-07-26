package com.market.repository;

import com.market.model.Item;
import com.market.model.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByShopId(Long shopId);

    List<Item> findByMediaType(MediaType mediaType);

    List<Item> findByShopIdAndMediaType(Long shopId, MediaType mediaType);

    List<Item> findByDescriptionContainingIgnoreCase(String name);

    List<Item> findByPriceBetween(Double minPrice, Double maxPrice);

    // Pageable versions
    Page<Item> findByShopId(Long shopId, Pageable pageable);

    Page<Item> findByMediaType(MediaType mediaType, Pageable pageable);

    Page<Item> findByShopIdAndMediaType(Long shopId, MediaType mediaType, Pageable pageable);

    Page<Item> findByDescriptionContainingIgnoreCase(String name, Pageable pageable);

    Page<Item> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);
}

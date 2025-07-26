package com.market.repository;

import com.market.model.Shop;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT shop FROM Shop shop WHERE shop.id = ?1")
    Optional<Shop> findByIdWithLock(Long id);

    List<Shop> findByOwnerId(Long ownerId);

    List<Shop> findByTownId(Long townId);

    List<Shop> findByCategoryId(Long categoryId);

    // Pageable versions
    Page<Shop> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Shop> findByTownId(Long townId, Pageable pageable);

    Page<Shop> findByCategoryId(Long categoryId, Pageable pageable);

    boolean existsByNameAndOwnerId(String name, Long ownerId);
}
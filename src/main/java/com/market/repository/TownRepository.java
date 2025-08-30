package com.market.repository;

import com.market.model.Town;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TownRepository extends JpaRepository<Town, Long> {

    @Query("SELECT t FROM Town t WHERE t.name LIKE %?1%")
    Page<Town> findByContainName(String name, Pageable pageable);

    boolean existsByName(String name);
    
    Town findByName(String name);
}

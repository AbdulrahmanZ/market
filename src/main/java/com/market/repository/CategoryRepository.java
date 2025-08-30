package com.market.repository;

import com.market.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    @Query("SELECT c FROM Category c WHERE c.name LIKE %?1%")
    Page<Category> findByContainName(String name, Pageable pageable);
    
    Category findByName(String name);
}

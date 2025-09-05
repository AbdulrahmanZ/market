package com.market.repository;

import com.market.model.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParameterRepository extends JpaRepository<Parameter, Long> {

    Parameter findByCode(String code);

    List<Parameter> findByCodeIn(List<String> codes);

    Boolean existsByCode(String code);

    @Modifying
    @Query("update Parameter p SET p.deleted = true WHERE p.id = :id")
    void softDeleteById(Long id);
}

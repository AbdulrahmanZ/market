package com.market.model;

import com.market.model.base.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "_categories")
public class Category extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String name;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

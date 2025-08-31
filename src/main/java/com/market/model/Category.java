package com.market.model;

import com.market.model.base.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "_categories")
public class Category extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true)
    private String code;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

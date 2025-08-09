package com.market.model;

import com.market.model.base.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "_towns")
public class Town extends BaseEntity {

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

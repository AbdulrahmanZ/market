package com.market.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.market.model.base.BaseEntity;
import com.market.serializer.TownSerializer;
import jakarta.persistence.*;

@Entity
@Table(name = "_towns")
@JsonSerialize(using = TownSerializer.class)
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

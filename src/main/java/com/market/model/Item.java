package com.market.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.market.model.base.BaseEntity;
import com.market.serializer.item.ItemSerializer;
import com.market.serializer.shop.IdLabelShopSerializer;
import jakarta.persistence.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "_items")
@JsonSerialize(using = ItemSerializer.class)
public class Item extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column
    private String currencyType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @JsonSerialize(using = IdLabelShopSerializer.class)
    private Shop shop;

    private String imageKeys;

    // Getters and Setters
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    @JsonIgnore
    public String getImageKeys() {
        return imageKeys;
    }

    public List<String> getImageKeysAsList() {
        if (this.imageKeys == null || this.imageKeys.trim().isEmpty()) {
            return List.of(); // Return an empty, immutable list for consistency
        }
        // Todo make it dynamic fetch
        return Arrays.stream(this.imageKeys.split(","))
                .map(String::trim)
                .limit(3)
                .collect(Collectors.toList());
    }

    public void setImageKeys(String imageKeys) {
        this.imageKeys = imageKeys;
    }

    public String getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }
}
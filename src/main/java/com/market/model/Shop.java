package com.market.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.market.model.base.BaseEntity;
import com.market.serializer.category.CategorySerializer;
import com.market.serializer.shop.ShopSerializer;
import com.market.serializer.town.TownSerializer;
import com.market.serializer.user.UserSerializer;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "_shops")
@JsonSerialize(using = ShopSerializer.class)
public class Shop extends BaseEntity {

    @Column(nullable = false)
    private String name;

    private String description;

    private String address;

    private String phone;

    private Integer itemLimit;

    @Column(columnDefinition = "boolean default true")
    private Boolean isActive;

    // Shop profile image
    private String profileImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonSerialize(using = CategorySerializer.class)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "town_id", nullable = false)
    @JsonSerialize(using = TownSerializer.class)
    private Town town;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonSerialize(using = UserSerializer.class)
    private User owner;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Item> items;

    public Shop(String name,
                String description,
                String address,
                String phone,
                Integer itemLimit,
                String profileImageUrl,
                Category category,
                Town town,
                User owner) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.itemLimit = itemLimit;
        this.profileImageUrl = profileImageUrl;
        this.category = category;
        this.town = town;
        this.owner = owner;
    }

    public Shop() {

    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getItemLimit() {
        return itemLimit;
    }

    public void setItemLimit(Integer itemLimit) {
        this.itemLimit = itemLimit;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Town getTown() {
        return town;
    }

    public void setTown(Town town) {
        this.town = town;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Integer getItemCount() {
        if (items == null)
            return 0;
        return items.size();
    }
}
package com.market.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public class ShopRequest {


    @NotBlank(message = "Shop name is required")
    private String name;
    
    private String description;
    private String address;
    private String phone;

    @Min(value = 1, message = "Item limit must be at least 1")
    private Integer itemLimit;
    
    @NotBlank(message = "Category name is required")
    private String categoryName;
    
    @NotBlank(message = "Town name is required")
    private String townName;
    
    @NotNull(message = "Owner ID is required")
    private Long ownerId;

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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getTownName() {
        return townName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

}

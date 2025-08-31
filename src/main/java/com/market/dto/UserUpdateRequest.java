package com.market.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;

public class UserUpdateRequest {
    
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Size(min = 5, message = "Password must be at least 5 characters")
    private String password;

    @Min(value = 1, message = "Shop limit must be at least 1")
    private Integer shopLimit;

    private Boolean isActive;

    private Boolean admin;

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getShopLimit() {
        return shopLimit;
    }

    public void setShopLimit(Integer shopLimit) {
        this.shopLimit = shopLimit;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }
}

package com.market.dto;

public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String username;
    private String phone;
    private Boolean admin;
    private Boolean isActive;

    public LoginResponse() {}

    public LoginResponse(String token, String type, Long userId, String username, String phone) {
        this.token = token;
        this.type = type;
        this.userId = userId;
        this.username = username;
        this.phone = phone;
    }

    public LoginResponse(String token, Long userId, String username, String phone, Boolean admin, Boolean isActive) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.phone = phone;
        this.admin = admin;
        this.isActive = isActive;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}

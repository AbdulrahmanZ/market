package com.market.dto;

import jakarta.validation.constraints.NotBlank;

// This DTO (Data Transfer Object) is used to receive data for creating or updating a Parameter.
// It includes validation constraints to ensure the data is not null or blank.
public class ParameterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Code is required")
    private String code;

    private String value; // Parameter has a 'value' field as requested.

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

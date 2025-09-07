package com.market.projection;

import java.util.List;

public interface ItemProjection {

    Long getId();

    String getName();

    String getDescription();

    Double getPrice();

    String getCurrencyType();

    Boolean getDeleted();

    List<String> getImageKeysAsList(); // Note: Use the getter method name for lists

    ShopInfo getShop();

    interface ShopInfo {
        Long getId();
        String getName();
        String getPhone();
    }
}
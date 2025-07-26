package com.market.exception;

public class ItemLimitExceededException extends RuntimeException {
    private final Long shopId;
    private final String shopName;
    private final Integer itemLimit;
    private final Integer currentCount;

    public ItemLimitExceededException(Long shopId, String shopName, Integer itemLimit, Integer currentCount) {
        super(String.format("Cannot add item to shop '%s'. Item limit of %d has been reached. Current items: %d", 
              shopName, itemLimit, currentCount));
        this.shopId = shopId;
        this.shopName = shopName;
        this.itemLimit = itemLimit;
        this.currentCount = currentCount;
    }

    public Long getShopId() {
        return shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public Integer getItemLimit() {
        return itemLimit;
    }

    public Integer getCurrentCount() {
        return currentCount;
    }
}

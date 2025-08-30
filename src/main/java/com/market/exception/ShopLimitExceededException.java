package com.market.exception;

public class ShopLimitExceededException extends RuntimeException {

    public ShopLimitExceededException(String message) {
        super(message);
    }

    public ShopLimitExceededException(Long userId, Integer shopLimit, Long currentCount) {
        super("User with ID " + userId + " has reached their shop limit of " + shopLimit + " shops. Current count: " + currentCount);
    }
}
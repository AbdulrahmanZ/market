package com.market.service;

import com.market.model.User;
import com.market.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Autowired
    UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String phone = authentication.getName();// Phone number is used as username
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public boolean isCurrentUser(Long userId) {
        try {
            return getCurrentUserId().equals(userId);
        } catch (Exception e) {
            return false;
        }
    }

    public void requireAuthentication() {
        getCurrentUser(); // This will throw exception if not authenticated
    }

    public void requireOwnership(Long ownerId) {
        if (!isCurrentUser(ownerId)) {
            throw new RuntimeException("Access denied: You can only modify your own resources");
        }
    }

    public void adminUserCheck() {
        if (!getCurrentUser().getAdmin()) {
            throw new RuntimeException("Unauthorized: Non-admin user");
        }
    }
}

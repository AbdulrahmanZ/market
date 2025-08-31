package com.market.controller;

import com.market.model.User;
import com.market.dto.UserRequest;
import com.market.dto.UserUpdateRequest;
import com.market.service.AuthenticationService;
import com.market.service.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final AuthenticationService authenticationService;

    public UserController(UserService userService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<User> createUser(@Valid @RequestBody UserRequest userRequest) {
        try {
            logger.info("Creating new user with username: {}", userRequest.getUsername());
            authenticationService.adminUserCheck();
            
            User createdUser = userService.createUserFromRequest(userRequest);
            logger.info("Successfully created user with ID: {}", createdUser.getId());
            
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            logger.error("Error creating user: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        try {
            logger.debug("Fetching user with ID: {}", id);
            authenticationService.adminUserCheck();
            
            User user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Error fetching user with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch user: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
        try {
            logger.debug("Fetching all users with pagination");
            authenticationService.adminUserCheck();
            
            Page<User> users = userService.getAllUsers(pageable);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error fetching users: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch users: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<User> updateUser(@PathVariable Long id, 
                                         @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        try {
            logger.info("Updating user with ID: {}", id);
            authenticationService.adminUserCheck();
            
            User updatedUser = userService.updateUser(id, userUpdateRequest);
            logger.info("Successfully updated user with ID: {}", id);
            
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("Error updating user with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update user: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            logger.info("Deleting user with ID: {}", id);
            authenticationService.adminUserCheck();
            
            userService.deleteUser(id);
            logger.info("Successfully deleted user with ID: {}", id);
            
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting user with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/shop-limit")
    @Transactional
    public ResponseEntity<User> updateShopLimit(@PathVariable Long id, 
                                             @RequestParam @Min(value = 1, message = "Shop limit must be at least 1") Integer shopLimit) {
        try {
            logger.info("Updating shop limit for user with ID: {} to {}", id, shopLimit);
            authenticationService.adminUserCheck();
            
            User updatedUser = userService.updateShopLimit(id, shopLimit);
            logger.info("Successfully updated shop limit for user with ID: {}", id);
            
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("Error updating shop limit for user with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update shop limit: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/activate")
    @Transactional
    public ResponseEntity<User> activateUser(@PathVariable Long id) {
        try {
            logger.info("Activating user with ID: {}", id);
            authenticationService.adminUserCheck();
            
            UserUpdateRequest updateRequest = new UserUpdateRequest();
            updateRequest.setIsActive(true);
            
            User updatedUser = userService.updateUser(id, updateRequest);
            logger.info("Successfully activated user with ID: {}", id);
            
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("Error activating user with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to activate user: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/deactivate")
    @Transactional
    public ResponseEntity<User> deactivateUser(@PathVariable Long id) {
        try {
            logger.info("Deactivating user with ID: {}", id);
            authenticationService.adminUserCheck();
            
            UserUpdateRequest updateRequest = new UserUpdateRequest();
            updateRequest.setIsActive(false);
            
            User updatedUser = userService.updateUser(id, updateRequest);
            logger.info("Successfully deactivated user with ID: {}", id);
            
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("Error deactivating user with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to deactivate user: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/grant-admin")
    @Transactional
    public ResponseEntity<User> grantAdmin(@PathVariable Long id) {
        try {
            logger.info("Granting admin privileges to user with ID: {}", id);
            authenticationService.adminUserCheck();
            
            UserUpdateRequest updateRequest = new UserUpdateRequest();
            updateRequest.setAdmin(true);
            
            User updatedUser = userService.updateUser(id, updateRequest);
            logger.info("Successfully granted admin privileges to user with ID: {}", id);
            
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("Error granting admin privileges to user with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to grant admin privileges: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/revoke-admin")
    @Transactional
    public ResponseEntity<User> revokeAdmin(@PathVariable Long id) {
        try {
            logger.info("Revoking admin privileges from user with ID: {}", id);
            authenticationService.adminUserCheck();
            
            UserUpdateRequest updateRequest = new UserUpdateRequest();
            updateRequest.setAdmin(false);
            
            User updatedUser = userService.updateUser(id, updateRequest);
            logger.info("Successfully revoked admin privileges from user with ID: {}", id);
            
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("Error revoking admin privileges from user with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to revoke admin privileges: " + e.getMessage());
        }
    }
}

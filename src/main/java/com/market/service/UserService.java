package com.market.service;

import com.market.model.User;
import com.market.repository.UserRepository;
import com.market.exception.UserAlreadyExistsException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.market.dto.UserRequest;
import com.market.dto.UserUpdateRequest;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException("username", user.getUsername());
        }
        if (userRepository.existsByPhone(user.getPhone())) {
            throw new UserAlreadyExistsException("phone", user.getPhone());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User createUserFromRequest(UserRequest userRequest) {
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new UserAlreadyExistsException("username", userRequest.getUsername());
        }
        if (userRepository.existsByPhone(userRequest.getPhone())) {
            throw new UserAlreadyExistsException("phone", userRequest.getPhone());
        }

        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setPhone(userRequest.getPhone());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setShopLimit(userRequest.getShopLimit());
        user.setAdmin(false); // Default to non-admin
        user.setActive(true); // Default to active

        return userRepository.save(user);
    }

    public User updateUser(Long id, UserUpdateRequest userUpdateRequest) {
        User user = getUserById(id);

        // Update username if provided and check uniqueness
        if (StringUtils.hasText(userUpdateRequest.getUsername()) && 
            !userUpdateRequest.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(userUpdateRequest.getUsername())) {
                throw new UserAlreadyExistsException("username", userUpdateRequest.getUsername());
            }
            user.setUsername(userUpdateRequest.getUsername());
        }

        // Update password if provided
        if (StringUtils.hasText(userUpdateRequest.getPassword())) {
            user.setPassword(passwordEncoder.encode(userUpdateRequest.getPassword()));
        }

        // Update shop limit if provided
        if (userUpdateRequest.getShopLimit() != null) {
            user.setShopLimit(userUpdateRequest.getShopLimit());
        }

        // Update active status if provided
        if (userUpdateRequest.getIsActive() != null) {
            user.setActive(userUpdateRequest.getIsActive());
        }

        // Update admin status if provided
        if (userUpdateRequest.getAdmin() != null) {
            user.setAdmin(userUpdateRequest.getAdmin());
        }

        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public void deleteUser(Long id) {
        userRepository.softDeleteById(id);
    }

    public Page<User> searchUsers(String phone, String username, Pageable pageable) {
        return userRepository.searchByPhoneAndUsername(phone, username, pageable);
    }

    public User updateShopLimit(Long userId, Integer newShopLimit) {
        User user = getUserById(userId);
        user.setShopLimit(newShopLimit);
        return userRepository.save(user);
    }
}
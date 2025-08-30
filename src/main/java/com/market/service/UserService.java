package com.market.service;

import com.market.model.User;
import com.market.repository.UserRepository;
import com.market.exception.UserAlreadyExistsException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
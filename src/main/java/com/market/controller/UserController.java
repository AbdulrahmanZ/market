package com.market.controller;

import com.market.model.User;
import com.market.service.AuthenticationService;
import com.market.service.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    AuthenticationService authenticationService;

    @PostMapping
    @Transactional
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        if (!authenticationService.getCurrentUser().getAdmin())
            throw new RuntimeException("Unauthorized - Non-admin user");
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
        Page<User> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!authenticationService.getCurrentUser().getAdmin())
            throw new RuntimeException("Unauthorized - Non-admin user");
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}

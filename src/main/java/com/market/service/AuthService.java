package com.market.service;

import com.market.dto.LoginRequest;
import com.market.dto.LoginResponse;
import com.market.dto.RegisterRequest;
import com.market.model.User;
import com.market.repository.UserRepository;
import com.market.exception.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public AuthService(
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Lazy AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService
    ) {
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("username", request.getUsername());
        }

        if (userRepository.existsByPhone(request.getPhone())) {
            throw new UserAlreadyExistsException("phone", request.getPhone());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPhone(request.getPhone());
        user.setAdmin(request.getAdmin());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getPhone());
        String jwtToken = jwtService.generateToken(userDetails);

        return new LoginResponse(jwtToken, savedUser.getId(), savedUser.getUsername(), savedUser.getPhone(), savedUser.getAdmin(), savedUser.getActive());
    }

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getPhone(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException("Invalid phone number or password");
        }

        User user = userRepository.findByPhone(request.getPhone())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getDeleted())
            throw new RuntimeException("This user has been deleted");

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getPhone());
        String jwtToken = jwtService.generateToken(userDetails);

        return new LoginResponse(jwtToken, user.getId(), user.getUsername(), user.getPhone(), user.getAdmin(), user.getActive());
    }

}

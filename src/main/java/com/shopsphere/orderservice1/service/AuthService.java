package com.shopsphere.orderservice1.service;

import com.shopsphere.orderservice1.dto.SignupRequest;
import com.shopsphere.orderservice1.dto.SignupResponse;
import com.shopsphere.orderservice1.entity.User;
import com.shopsphere.orderservice1.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (request == null || request.email() == null
                || request.password() == null) {
            throw new IllegalArgumentException(
                "Email and password are required");
        }

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String password = request.password();

        if (email.isBlank() || email.length() > 254
                || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Enter a valid email address");
        }

        if (password.length() < 8 || password.length() > 72) {
            throw new IllegalArgumentException(
                "Password must be between 8 and 72 characters");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                "An account with this email already exists");
        }

        String passwordHash = passwordEncoder.encode(password);
        User user = new User(email, passwordHash);
        User savedUser = userRepository.save(user);

        return new SignupResponse(
            savedUser.getId(),
            savedUser.getEmail(),
            "Account created successfully");
    }
}

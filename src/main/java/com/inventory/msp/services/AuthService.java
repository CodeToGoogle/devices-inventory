package com.inventory.msp.services;

import com.inventory.msp.model.AppUser;

import com.inventory.msp.model.UserRole;
import com.inventory.msp.repository.UserRepository;
import com.inventory.msp.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String login(String username, String password) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
    }

    public AppUser createUser(String username, String rawPassword, UserRole role, String agencyName) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("User already exists");
        }
        AppUser u = AppUser.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .agencyName(agencyName)
                .build();
        return userRepository.save(u);
    }
}

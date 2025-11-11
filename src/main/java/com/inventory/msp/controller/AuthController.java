package com.inventory.msp.controller;

import com.inventory.msp.dto.AuthRequest;
import com.inventory.msp.dto.AuthResponse;
import com.inventory.msp.model.UserRole;
import com.inventory.msp.model.AppUser;
import com.inventory.msp.services.AuthService;
import com.inventory.msp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
        String token = authService.login(req.getUsername(), req.getPassword());
        AppUser u = userRepository.findByUsername(req.getUsername()).get();
        return ResponseEntity.ok(new AuthResponse(token, u.getUsername(), u.getRole().name()));
    }

    // ADMIN-only in SecurityConfig; but you can restrict additionally
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestParam String username,
                                        @RequestParam String password,
                                        @RequestParam String role,
                                        @RequestParam(required = false) String agencyName) {

        UserRole r = UserRole.valueOf(role.toUpperCase());
        AppUser u = authService.createUser(username, password, r, agencyName);
        return ResponseEntity.ok(u);
    }
}

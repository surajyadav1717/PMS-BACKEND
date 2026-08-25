package com.example.pms.api;

import com.example.pms.repository.AppUserRepository;
import com.example.pms.security.JwtService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(AppUserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users; this.encoder = encoder; this.jwt = jwt;
    }

    public record LoginRequest(@NotBlank String username, String password) {}
    public record LoginResponse(String token, String username, String role, Long employeeId) {}

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        var user = users.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!encoder.matches(request.password(), user.getPasswordHash()) || !"ACTIVE".equals(user.getStatus())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return new LoginResponse(
                jwt.generate(user.getUsername(), user.getRole().name()),
                user.getUsername(), user.getRole().name(),
                user.getEmployee() == null ? null : user.getEmployee().getId());
    }
}

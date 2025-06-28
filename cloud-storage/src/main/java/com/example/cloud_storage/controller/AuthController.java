package com.example.cloud_storage.controller;

import com.example.cloud_storage.dto.AuthRequest;
import com.example.cloud_storage.dto.AuthResponse;
import com.example.cloud_storage.dto.ErrorResponse;
import com.example.cloud_storage.exception.CloudStorageException;
import com.example.cloud_storage.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@RestController
@Slf4j
public class AuthController {

    private final AuthService authService;
    private static int errorIdCounter = 0;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            // Используем геттеры из DTO AuthRequest
            String authToken = authService.login(authRequest.getLogin(), authRequest.getPassword());
            log.info("User '{}' logged in successfully.", authRequest.getLogin());
            // В случае успеха возвращаем DTO AuthResponse
            return ResponseEntity.ok(new AuthResponse(authToken));

        } catch (CloudStorageException ex) {
            log.warn("Login failed for user '{}': {}", authRequest.getLogin(), ex.getMessage());
            // В случае ошибки возвращаем ErrorResponse
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(ex.getMessage(), ++errorIdCounter));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("auth-token") String authToken) {
        try {
            authService.logout(authToken);
            log.info("User with token prefix '{}...' logged out.", authToken.substring(0, Math.min(authToken.length(), 15)));
            return ResponseEntity.ok().build();
        } catch (CloudStorageException ex) {
            log.warn("Logout failed for token prefix '{}...': {}", authToken.substring(0, Math.min(authToken.length(), 15)), ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
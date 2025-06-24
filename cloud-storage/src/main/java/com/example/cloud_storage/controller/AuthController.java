package com.example.cloud_storage.controller;

import com.example.cloud_storage.dto.AuthRequest;
import com.example.cloud_storage.dto.AuthResponse;
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


@Slf4j
@RequiredArgsConstructor
@RestController // Помечает класс как REST контроллер
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            String authToken = authService.login(authRequest.getLogin(), authRequest.getPassword());
            return ResponseEntity.ok(new AuthResponse(authToken));

        } catch (CloudStorageException ex) {

            log.warn("Login failed for user {}: {}", authRequest.getLogin(), ex.getMessage());
            // ошибка.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponse(ex.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("auth-token") String authToken) {
        try {
            authService.logout(authToken);
            return ResponseEntity.ok().build();
        } catch (CloudStorageException ex) {
            // Если токен не найден, сервис выбросит исключение.
            // 401 Unauthorized.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
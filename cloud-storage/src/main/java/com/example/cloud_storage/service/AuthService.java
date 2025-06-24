package com.example.cloud_storage.service;

import com.example.cloud_storage.exception.CloudStorageException;
import com.example.cloud_storage.model.User;
import com.example.cloud_storage.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

@Service
@Slf4j // Lombok аннотация для автоматического создания логгера
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String login(String username, String password) {
        log.info("Attempting login for user '{}'", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    // Важно: не сообщайте, что именно не так - логин или пароль.
                    log.warn("Login failed for non-existent user '{}'", username);
                    return new CloudStorageException("Bad credentials");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Login failed for user '{}' due to invalid password", username);
            throw new CloudStorageException("Bad credentials");
        }

        String authToken = UUID.randomUUID().toString();
        user.setAuthToken(authToken);
        userRepository.save(user);
        // Не логируем полный токен из соображений безопасности.
        log.info("User '{}' successfully logged in. Token suffix: ...{}", username, authToken.substring(authToken.length() - 4));

        return authToken;
    }

    @Transactional
    public void logout(String authToken) {
        log.info("Attempting logout for token suffix: ...{}", authToken.substring(authToken.length() - 4));

        User user = userRepository.findByAuthToken(authToken)
                .orElseThrow(() -> {
                    log.warn("Logout failed: token not found. Suffix: ...{}", authToken.substring(authToken.length() - 4));
                    return new CloudStorageException("Unauthorized: Invalid auth token");
                });

        user.setAuthToken(null);
        userRepository.save(user);
        log.info("User '{}' successfully logged out.", user.getUsername());
    }


    public User getUserByAuthToken(String authToken) {
        log.debug("Fetching user by token suffix: ...{}", authToken.substring(authToken.length() - 4));
        return userRepository.findByAuthToken(authToken)
                .orElseThrow(() -> new CloudStorageException("Unauthorized: Invalid auth token"));
    }

    @Transactional
    public void registerUser(String username, String password) {
        log.info("Attempting to register new user '{}'", username);
        if (userRepository.findByUsername(username).isPresent()) {
            log.warn("Registration failed: username '{}' already exists.", username);
            throw new CloudStorageException("User with this username already exists");
        }
        User newUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build();
        userRepository.save(newUser);
        log.info("User '{}' registered successfully.", username);
    }

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException { // !!! Возвращаем ваш User
        log.debug("Spring Security is loading user by username: '{}'", username);
        // Возвращаем ваш объект User напрямую, т.к. он реализует UserDetails
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Spring Security failed to find user '{}'", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });
        return user;
    }
}





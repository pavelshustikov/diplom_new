package com.example.cloud_storage.filter;

import com.example.cloud_storage.model.User;
import com.example.cloud_storage.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j // Для логирования
public class AuthTokenFilter extends OncePerRequestFilter {

    private final AuthService authService;

    public AuthTokenFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String authToken = request.getHeader("auth-token"); // Получаем токен из заголовка

            if (authToken != null && !authToken.isEmpty()) {
                log.debug("AuthToken found: {}", authToken);
                User user = authService.getUserByAuthToken(authToken); // Ищем пользователя по токену

                if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Создаем объект аутентификации Spring Security

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            user, null, Collections.emptyList()); // У пользователя пока нет ролей

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication); // Устанавливаем аутентификацию в контекст
                    log.debug("User {} authenticated with token.", user.getUsername());
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            // Можно отправить 401 ошибку здесь, но Spring Security сам это обработает через AuthenticationEntryPoint
        }

        filterChain.doFilter(request, response); // Передаем запрос дальше по цепочке фильтров
    }
}
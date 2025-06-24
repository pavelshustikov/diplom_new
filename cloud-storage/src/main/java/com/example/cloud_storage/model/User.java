package com.example.cloud_storage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails; // !!! Импортируем UserDetails
import java.util.Collection;
import java.util.Collections; // Для простого списка ролей

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails { // !!! Теперь User реализует UserDetails

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // Заметьте, что это поле также требуется UserDetails

    @Column(name = "auth_token", unique = true)
    private String authToken;

    @Column(name = "token_creation_time")
    private Long tokenCreationTime;

    // --- Методы из интерфейса UserDetails ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Здесь можно вернуть роли пользователя, например:
        // return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        // Для вашей текущей реализации, где ролей нет, достаточно пустого списка.
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() {
        // Верните true, если аккаунт никогда не истекает
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Верните true, если аккаунт никогда не блокируется
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Верните true, если учетные данные (пароль) никогда не истекают
        // Здесь вы могли бы проверять `tokenCreationTime` для управления сроком жизни токена
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Верните true, если аккаунт активен
        return true;
    }

    // Lombok уже генерирует геттеры и сеттеры для `username` и `password`,
    // которые используются `UserDetails`.
}



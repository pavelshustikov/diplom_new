package com.example.cloud_storage.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.example.cloud_storage.model.File;
import com.example.cloud_storage.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // Загружает только JPA-компоненты. Идеально для тестов репозитория
@Testcontainers // Включает поддержку Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Отключаем встроенную БД
@ActiveProfiles("test")
class FileRepositoryTest {

    @Container // Создает и управляет контейнером
    private static final PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository; // Также нужен для создания пользователя

    // Динамически передаем Spring'у URL, логин и пароль от запущенного контейнера
    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
    }

    // Перед каждым тестом сохраняем пользователя, чтобы не нарушать foreign key constraint
    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password"); // В реальном проекте пароль должен быть захеширован
        userRepository.save(testUser);
    }
    @Test
    void findByFilenameAndUser_ShouldReturnFile_WhenFileExists() { // Название теста тоже лучше поправить для ясности
        // --- Arrange ---
        User user = userRepository.findByUsername("testuser").get();
        File file = new File();
        file.setFilename("document.pdf");
        // ИЗМЕНЕНИЕ 1: Используем setUser вместо setOwner
        file.setUser(user);
        file.setSize(1024L);
        // ПРЕДПОЛОЖЕНИЕ: Метод для установки контента называется setContent
        file.setContent(new byte[]{1, 2, 3});

        fileRepository.save(file);

        // --- Act ---
        // ИЗМЕНЕНИЕ 2: Вызываем правильный метод репозитория
        Optional<File> foundFile = fileRepository.findByFilenameAndUser("document.pdf", user);

        // --- Assert ---
        assertTrue(foundFile.isPresent(), "Файл должен быть найден");
        assertEquals("document.pdf", foundFile.get().getFilename());
        // ИЗМЕНЕНИЕ 3: Используем getUser вместо getOwner
        assertEquals(user.getUsername(), foundFile.get().getUser().getUsername());
    }
}
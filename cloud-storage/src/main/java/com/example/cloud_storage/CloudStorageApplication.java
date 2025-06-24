package com.example.cloud_storage;

import com.example.cloud_storage.exception.CloudStorageException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.cloud_storage.service.FileService;
import com.example.cloud_storage.service.AuthService; // Добавляем импорт AuthService

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder; // Добавляем импорт PasswordEncoder

@SpringBootApplication
public class CloudStorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudStorageApplication.class, args);
	}


	@Bean
	public CommandLineRunner init(FileService fileService, AuthService authService) {
		return args -> {
			try {
				fileService.init(); // Инициализация папки для хранения файлов

				// Мы просто пытаемся зарегистрировать пользователей.
				// Если они уже существуют, метод registerUser выбросит исключение,
				// которое мы можем проигнорировать в блоке catch.
				try {
					authService.registerUser("user1", "password");
				} catch (CloudStorageException e) {
					// Игнорируем ошибку "пользователь уже существует" при запуске
					System.out.println("User 'user1' already exists.");
				}

				try {
					authService.registerUser("user2", "strong_password");
				} catch (CloudStorageException e) {
					System.out.println("User 'user2' already exists.");
				}

			} catch (Exception e) {
				// Логируем любую другую ошибку при инициализации
				// log.error("Initialization failed", e);
				System.err.println("Initialization failed: " + e.getMessage());
			}
		};
	}
}
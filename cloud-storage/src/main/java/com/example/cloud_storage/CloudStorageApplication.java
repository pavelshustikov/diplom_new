package com.example.cloud_storage;

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
	public CommandLineRunner init(FileService fileService, AuthService authService, PasswordEncoder passwordEncoder) {
		return args -> {
			fileService.init(); // Инициализация папки для хранения файлов

			if (authService.getUserRepository().findByUsername("user1").isEmpty()) { // Проверяем, что пользователя нет
				authService.registerUser("user1", "password");
			}
			if (authService.getUserRepository().findByUsername("user2").isEmpty()) {
				authService.registerUser("user2", "strong_password");
			}
		};
	}
}
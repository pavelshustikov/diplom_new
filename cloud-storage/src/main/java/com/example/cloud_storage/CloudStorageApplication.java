package com.example.cloud_storage;

import com.example.cloud_storage.exception.CloudStorageException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.example.cloud_storage.service.FileService;
import com.example.cloud_storage.service.AuthService; // Добавляем импорт AuthService
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder; // Добавляем импорт PasswordEncoder

@SpringBootApplication
@Slf4j
public class CloudStorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudStorageApplication.class, args);
	}


	@Bean
	public CommandLineRunner init(FileService fileService, AuthService authService) {
		return args -> {
			try {
				fileService.init();
				log.info("File storage successfully initialized.");

				try {
					authService.registerUser("user1", "password");
					log.info("Test user 'user1' was registered.");
				} catch (CloudStorageException e) {
					log.warn("Test user 'user1' already exists. Skipping registration.");
				}

				try {
					authService.registerUser("user2", "strong_password");
					log.info("Test user 'user2' was registered.");
				} catch (CloudStorageException e) {
					log.warn("Test user 'user2' already exists. Skipping registration.");
				}

			} catch (Exception e) {
				log.error("Failed to initialize application data.", e);
			}
		};
	}
}
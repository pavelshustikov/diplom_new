package com.example.cloud_storage.service;

import com.example.cloud_storage.dto.FileResponse;
import com.example.cloud_storage.exception.CloudStorageException;
import com.example.cloud_storage.model.File;
import com.example.cloud_storage.model.User;
import com.example.cloud_storage.repository.FileRepository;
import org.springframework.transaction.annotation.Transactional;
//import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import com.example.cloud_storage.repository.UserRepository; // Добавляем репозиторий для User


import javax.annotation.PostConstruct;

import java.util.stream.Collectors;


import com.example.cloud_storage.dto.FileResponse;
import com.example.cloud_storage.exception.CloudStorageException;
import com.example.cloud_storage.model.File;
import com.example.cloud_storage.model.User;
import com.example.cloud_storage.repository.FileRepository;
import com.example.cloud_storage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;



@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository; // UserRepository здесь нужен, возможно, для других операций, но не для получения текущего пользователя.

    @Value("${file.storage.location}")
    private String fileStorageLocation;

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(fileStorageLocation);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created storage directory: {}", fileStorageLocation);
            } else {
                log.info("Storage directory already exists: {}", fileStorageLocation);
            }
        } catch (IOException e) {
            log.error("Failed to create upload directory!", e);
            throw new CloudStorageException("Failed to create upload directory: " + e.getMessage());
        }
    }

    @Transactional
    public void uploadFile(String filename, MultipartFile multipartFile, User user) { // ПРИНИМАЕМ ОБЪЕКТ User
        log.info("User '{}' is attempting to upload file '{}'", user.getUsername(), filename);

        if (fileRepository.findByFilenameAndUser(filename, user).isPresent()) {
            log.warn("User '{}' failed to upload file. File '{}' already exists.", user.getUsername(), filename);
            throw new CloudStorageException("A file named '" + filename + "' already exists for this user.");
        }

        Path targetLocation = Paths.get(fileStorageLocation).resolve(user.getUsername() + "_" + filename);

        try {
            Files.copy(multipartFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            File fileEntity = File.builder()
                    .filename(filename)
                    .size(multipartFile.getSize())
                    .uploadDate(LocalDateTime.now())
                    .path(targetLocation.toString())
                    .user(user) // Привязываем файл к полученному объекту User
                    .build();
            fileRepository.save(fileEntity);
            log.info("File '{}' successfully uploaded by user '{}' to path {}", filename, user.getUsername(), targetLocation);

        } catch (IOException ex) {
            log.error("Failed to save file {} for user {}", filename, user.getUsername(), ex);
            throw new CloudStorageException("Could not save file " + filename + ". Please try again! " + ex.getMessage());
        }
    }

    @Transactional
    public void deleteFile(String filename, User user) { //  ПРИНИМАЕМ ОБЪЕКТ User
        log.info("User '{}' is attempting to delete file '{}'", user.getUsername(), filename);

        File fileEntity = fileRepository.findByFilenameAndUser(filename, user)
                .orElseThrow(() -> {
                    log.warn("User '{}' attempted to delete a non-existent file '{}'", user.getUsername(), filename);
                    return new CloudStorageException("File '" + filename + "' not found for this user.");
                });

        try {
            Path filePath = Paths.get(fileEntity.getPath());
            Files.deleteIfExists(filePath);
            fileRepository.delete(fileEntity);
            log.info("File '{}' successfully deleted by user '{}'", filename, user.getUsername());
        } catch (IOException ex) {
            log.error("Error while deleting file '{}' from disk for user '{}'", filename, user.getUsername(), ex);
            throw new CloudStorageException("Failed to delete file " + filename + ". " + ex.getMessage());
        }
    }

    /**
     * Извлекает список файлов для указанного пользователя с заданным лимитом.
     *
     * @param limit Максимальное количество файлов в списке.
     * @param user  Аутентифицированный объект пользователя (получен напрямую из контроллера).
     * @return Список объектов FileResponse.
     */
    @Transactional(readOnly = true)
    public List<FileResponse> getFileList(int limit, User user) { //  ПРИНИМАЕМ ОБЪЕКТ User
        log.info("User '{}' is requesting a file list with limit {}", user.getUsername(), limit);

        List<File> files = fileRepository.findByUser(user); // Используем уже имеющийся объект User

        log.info("Found {} total files for user '{}' before applying limit.", files.size(), user.getUsername());

        List<FileResponse> limitedFiles = files.stream()
                .limit(limit)
                .map(this::mapToFileResponse)
                .collect(Collectors.toList());

        log.info("Returning {} files for user '{}' after applying limit of {}.", limitedFiles.size(), user.getUsername(), limit);
        return limitedFiles;
    }

    private FileResponse mapToFileResponse(File file) {
        return FileResponse.builder()
                .filename(file.getFilename())
                .size(file.getSize())
                .build();
    }
}

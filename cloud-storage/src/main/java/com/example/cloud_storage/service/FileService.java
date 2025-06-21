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
@RequiredArgsConstructor // Used for dependency injection
public class FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository; // Inject UserRepository

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
    public void uploadFile(String filename, MultipartFile multipartFile, String username) {
        log.info("User '{}' is attempting to upload file '{}'", username, filename);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User '{}' not found during file upload.", username);
                    return new CloudStorageException("User '" + username + "' not found.");
                });

        if (fileRepository.findByFilenameAndUser(filename, user).isPresent()) {
            log.warn("User '{}' failed to upload file. File '{}' already exists.", username, filename);
            throw new CloudStorageException("A file named '" + filename + "' already exists for this user.");
        }

        // Create a unique path for the file using the username
        Path targetLocation = Paths.get(fileStorageLocation).resolve(user.getUsername() + "_" + filename);

        try {
            Files.copy(multipartFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            File fileEntity = File.builder()
                    .filename(filename)
                    .size(multipartFile.getSize())
                    .uploadDate(LocalDateTime.now())
                    .path(targetLocation.toString())
                    .user(user)
                    .build();
            fileRepository.save(fileEntity);
            log.info("File '{}' successfully uploaded by user '{}' to path {}", filename, username, targetLocation);

        } catch (IOException ex) {
            log.error("Failed to save file {} for user {}", filename, username, ex);
            throw new CloudStorageException("Could not save file " + filename + ". Please try again! " + ex.getMessage());
        }
    }

    @Transactional
    public void deleteFile(String filename, String username) {
        log.info("User '{}' is attempting to delete file '{}'", username, filename);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User '{}' not found during file deletion.", username);
                    return new CloudStorageException("User '" + username + "' not found.");
                });

        File fileEntity = fileRepository.findByFilenameAndUser(filename, user)
                .orElseThrow(() -> {
                    log.warn("User '{}' attempted to delete a non-existent file '{}'", username, filename);
                    return new CloudStorageException("File '" + filename + "' not found for this user.");
                });

        try {
            Path filePath = Paths.get(fileEntity.getPath());
            Files.deleteIfExists(filePath);
            fileRepository.delete(fileEntity);
            log.info("File '{}' successfully deleted by user '{}'", filename, username);
        } catch (IOException ex) {
            log.error("Error while deleting file '{}' from disk for user '{}'", filename, username, ex);
            throw new CloudStorageException("Failed to delete file " + filename + ". " + ex.getMessage());
        }
    }

    /**
     * Retrieves a list of files for the specified user with a given limit.
     *
     * @param limit    The maximum number of files in the list.
     * @param username The name of the user for whom to retrieve the file list.
     * @return A list of FileResponse objects.
     */
    @Transactional(readOnly = true) // Read-only transaction
    public List<FileResponse> getFileList(int limit, String username) {
        log.info("User '{}' is requesting a file list with limit {}", username, limit);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User '{}' not found when retrieving file list.", username);
                    return new CloudStorageException("User '" + username + "' not found.");
                });

        // In a real application, Spring Data JPA Pageable or Slice would be used
        // for efficient limiting and pagination.
        // Here, for simplicity, all files are fetched and then limited in memory.
        List<File> files = fileRepository.findByUser(user);

        log.info("Found {} total files for user '{}' before applying limit.", files.size(), username);

        List<FileResponse> limitedFiles = files.stream()
                .limit(limit)
                .map(this::mapToFileResponse)
                .collect(Collectors.toList());

        log.info("Returning {} files for user '{}' after applying limit of {}.", limitedFiles.size(), username, limit);
        return limitedFiles;
    }

    // Helper method to convert a File entity to a FileResponse DTO
    private FileResponse mapToFileResponse(File file) {
        return FileResponse.builder()
                .filename(file.getFilename())
                .size(file.getSize())
                .build();
    }
}

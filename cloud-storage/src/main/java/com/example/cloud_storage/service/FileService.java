package com.example.cloud_storage.service;

import com.example.cloud_storage.dto.FileResponse;
import com.example.cloud_storage.exception.CloudStorageException;
import com.example.cloud_storage.model.File;
import com.example.cloud_storage.model.User;
import com.example.cloud_storage.repository.FileRepository;
import jakarta.transaction.Transactional;
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
import java.util.stream.Collectors;

@Service
public class FileService {

    @Value("${file.storage.location}") // Инжектируем путь к папке для хранения файлов из application.yml
    private String fileStorageLocation;

    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    // Инициализация папки для хранения файлов при запуске
    public void init() {
        try {
            Path uploadPath = Paths.get(fileStorageLocation);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            throw new CloudStorageException("Could not create upload directory: " + e.getMessage());
        }
    }

    @Transactional
    public void uploadFile(String filename, MultipartFile file, User user) {

        if (fileRepository.findByFilenameAndUser(filename, user).isPresent()) {
            throw new CloudStorageException("File with name '" + filename + "' already exists for this user.");
        }

        try {
            Path targetLocation = Paths.get(fileStorageLocation).resolve(user.getUsername() + "_" + filename);

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);


            File fileEntity = File.builder()
                    .filename(filename)
                    .size(file.getSize())
                    .uploadDate(LocalDateTime.now())
                    .path(targetLocation.toString())
                    .user(user)
                    .build();
            fileRepository.save(fileEntity);

        } catch (IOException ex) {
            throw new CloudStorageException("Could not store file " + filename + ". Please try again! " + ex.getMessage());
        }
    }

    @Transactional
    public void deleteFile(String filename, User user) {
        File fileEntity = fileRepository.findByFilenameAndUser(filename, user)
                .orElseThrow(() -> new CloudStorageException("File '" + filename + "' not found for this user."));

        try {
            Path filePath = Paths.get(fileEntity.getPath());
            Files.deleteIfExists(filePath); // Удаляем файл с диска
            fileRepository.delete(fileEntity); // Удаляем запись из БД
        } catch (IOException ex) {
            throw new CloudStorageException("Could not delete file " + filename + ". " + ex.getMessage());
        }
    }

    @Transactional
    public List<FileResponse> getFileList(int limit, User user) {

        List<File> files = fileRepository.findByUser(user, PageRequest.of(0, limit));

        return files.stream()
                .map(file -> new FileResponse(file.getFilename(), file.getSize()))
                .collect(Collectors.toList());
    }

    public void setFileStorageLocation(String fileStorageLocation) {
        this.fileStorageLocation = fileStorageLocation;
    }
}
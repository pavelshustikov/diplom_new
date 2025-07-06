package com.example.cloud_storage.controller;

import com.example.cloud_storage.dto.FileEditRequest;
import com.example.cloud_storage.dto.FileResponse;
import com.example.cloud_storage.model.User;
import com.example.cloud_storage.service.AuthService;
import com.example.cloud_storage.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


@Slf4j
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * Загружает файл для текущего аутентифицированного пользователя.
     *
     * @param user Аутентифицированный объект пользователя. Внедряется Spring Security.
     * @param file Файл для загрузки.
     * @return ResponseEntity со статусом 200 OK в случае успеха.
     */
    @PostMapping("/file")
    //@GetMapping ("/file")
    public ResponseEntity<Void> uploadFile(
            @AuthenticationPrincipal User user, // ПРИНИМАЕМ ОБЪЕКТ User
            @RequestParam("file") MultipartFile file
    ) {
        String username = user.getUsername(); // Получаем имя пользователя из объекта User
        log.info("Request to upload file '{}' from user '{}'", file.getOriginalFilename(), username);

        fileService.uploadFile(file.getOriginalFilename(), file, user); //ПЕРЕДАЕМ ОБЪЕКТ User в сервис

        log.info("Successfully processed upload request for file '{}' from user '{}'", file.getOriginalFilename(), username);
        return ResponseEntity.ok().build();
    }

    /**
     * Удаляет файл для текущего аутентифицированного пользователя.
     *
     * @param user Аутентифицированный объект пользователя.
     * @param filename Имя файла для удаления.
     * @return ResponseEntity со статусом 200 OK в случае успеха.
     */
    @DeleteMapping("/file")
    public ResponseEntity<Void> deleteFile(
            @AuthenticationPrincipal User user, // !!! ПРИНИМАЕМ ОБЪЕКТ User
            @RequestParam("filename") String filename
    ) {
        String username = user.getUsername();
        log.info("Request to delete file '{}' from user '{}'", filename, username);

        fileService.deleteFile(filename, user); // ПЕРЕДАЕМ ОБЪЕКТ User в сервис

        log.info("Successfully processed delete request for file '{}' from user '{}'", filename, username);
        return ResponseEntity.ok().build();
    }

    /**
     * Извлекает список файлов для текущего аутентифицированного пользователя.
     *
     * @param user Аутентифицированный объект пользователя.
     * @param limit Максимальное количество файлов в списке.
     * @return ResponseEntity со списком файлов и статусом 200 OK.
     */
    @GetMapping("/list")
    public ResponseEntity<List<FileResponse>> getFileList(
            @AuthenticationPrincipal User user, //ПРИНИМАЕМ ОБЪЕКТ User
            @RequestParam("limit") int limit
    ) {
        String username = user.getUsername();
        log.info("Request to get file list (limit: {}) from user '{}'", limit, username);

        // ПЕРЕДАЕМ ОБЪЕКТ User в сервис, избегая повторного поиска
        List<FileResponse> files = fileService.getFileList(limit, user);

        log.info("Successfully processed request for file list for user '{}'. Found {} files.", username, files.size());
        return ResponseEntity.ok(files);
    }

    /**
     * Редактирует (переименовывает) файл.
     * Принимает старое имя в query-параметре и новое имя в теле запроса.
     */
    @PutMapping("/file")
    public ResponseEntity<Void> editFile(
            @AuthenticationPrincipal User user,
            @RequestParam("filename") String oldFilename,
            @RequestBody FileEditRequest request
    ) {
        String newFilename = request.getFilename();
        log.info("Request from user '{}' to rename file '{}' to '{}'", user.getUsername(), oldFilename, newFilename);
        fileService.editFileName(oldFilename, newFilename, user);
        log.info("Successfully processed rename request for user '{}'", user.getUsername());
        return ResponseEntity.ok().build();
    }
}



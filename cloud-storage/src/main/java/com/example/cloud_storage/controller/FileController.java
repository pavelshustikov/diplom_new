package com.example.cloud_storage.controller;

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
import java.security.Principal;


/**
 * Controller for managing user files.
 *
 * This controller uses standard Spring Security mechanisms for authentication.
 * Instead of manual token handling, it retrieves user information
 * directly from the Principal object injected by Spring MVC.
 */
@Slf4j
@RestController
@RequiredArgsConstructor // Used for constructor-based dependency injection
public class FileController {

    private final FileService fileService;

    /**
     * Uploads a file for the currently authenticated user.
     *
     * @param principal The object containing authenticated user data. Injected automatically.
     * @param file      The file to be uploaded.
     * @return ResponseEntity with a 200 OK status on success.
     */
    @PostMapping("/file")
    public ResponseEntity<Void> uploadFile(
            Principal principal,
            @RequestParam("file") MultipartFile file
    ) {
        String username = principal.getName();
        log.info("Request to upload file '{}' from user '{}'", file.getOriginalFilename(), username);

        fileService.uploadFile(file.getOriginalFilename(), file, username);

        log.info("Successfully processed upload request for file '{}' from user '{}'", file.getOriginalFilename(), username);
        return ResponseEntity.ok().build();
    }

    /**
     * Deletes a file for the currently authenticated user.
     *
     * @param principal The object containing authenticated user data.
     * @param filename  The name of the file to delete.
     * @return ResponseEntity with a 200 OK status on success.
     */
    @DeleteMapping("/file")
    public ResponseEntity<Void> deleteFile(
            Principal principal,
            @RequestParam("filename") String filename
    ) {
        String username = principal.getName();
        log.info("Request to delete file '{}' from user '{}'", filename, username);

        fileService.deleteFile(filename, username);

        log.info("Successfully processed delete request for file '{}' from user '{}'", filename, username);
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves a list of files for the currently authenticated user.
     *
     * @param principal The object containing authenticated user data.
     * @param limit     The maximum number of files in the list.
     * @return ResponseEntity with a list of files and a 200 OK status.
     */
    @GetMapping("/list")
    public ResponseEntity<List<FileResponse>> getFileList(
            Principal principal,
            @RequestParam("limit") int limit
    ) {
        String username = principal.getName();
        log.info("Request to get file list (limit: {}) from user '{}'", limit, username);

        List<FileResponse> files = fileService.getFileList(limit, username);

        log.info("Successfully processed request for file list for user '{}'. Found {} files.", username, files.size());
        return ResponseEntity.ok(files);
    }
}
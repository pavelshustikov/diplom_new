package com.example.cloud_storage.service;
import com.example.cloud_storage.exception.CloudStorageException;
import com.example.cloud_storage.model.File;
import com.example.cloud_storage.model.User;
import com.example.cloud_storage.service.FileService;
import com.example.cloud_storage.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

// Включаем поддержку Mockito для создания моков
@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    // Создаем "заглушку" для репозитория. Мы будем контролировать его поведение.
    @Mock
    private FileRepository fileRepository;

    // Внедряем созданный мок в наш сервис.
    // Обратите внимание: т.к. мы не используем Spring Context, нам нужно вручную установить поле fileStorageLocation.
    @InjectMocks
    private FileService fileService;

    // Подготовим тестовые данные, которые будут использоваться в нескольких тестах
    private User testUser;
    private MockMultipartFile multipartFile;
    private final String filename = "test.txt";

    @BeforeEach
    void setUp() throws IOException {
        // Устанавливаем значение для fileStorageLocation вручную.
        // В тестах мы работаем с временной директорией, чтобы не засорять систему.
        Path tempDir = Files.createTempDirectory("test-uploads");
        fileService.setFileStorageLocation(tempDir.toString()); // Вам нужно будет добавить сеттер в FileService

        // Инициализируем сервис, чтобы он создал временную папку
        fileService.init();

        // Создаем тестового пользователя
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setId(1L);

        // Создаем тестовый файл
        multipartFile = new MockMultipartFile(
                "file", // имя параметра в запросе
                filename, // оригинальное имя файла
                "text/plain",
                "some test content".getBytes()
        );
    }

    @Test
    void uploadFile_ShouldSaveFile_WhenFileDoesNotExist() {
        // --- Arrange (Подготовка) ---
        // Настраиваем поведение мока: когда репозиторий спросят о файле, он скажет, что такого файла нет.
        when(fileRepository.findByFilenameAndUser(filename, testUser)).thenReturn(Optional.empty());

        // --- Act (Действие) ---
        // Вызываем тестируемый метод
        fileService.uploadFile(filename, multipartFile, testUser);

        // --- Assert (Проверка) ---
        // Проверяем, что метод save у репозитория был вызван ровно 1 раз
        // с любым объектом типа File (any(File.class)).
        verify(fileRepository, times(1)).save(any(File.class));
    }

    @Test
    void uploadFile_ShouldThrowException_WhenFileAlreadyExists() {
        // --- Arrange ---
        // На этот раз настраиваем мок так, чтобы он "нашел" файл в базе.
        // Мы передаем new File() просто как заглушку, его содержимое не важно.
        when(fileRepository.findByFilenameAndUser(filename, testUser)).thenReturn(Optional.of(new File()));

        // --- Act & Assert ---
        // Проверяем, что вызов метода uploadFile с этими данными приведет к выбросу исключения CloudStorageException.
        // Это правильное поведение, которое мы ожидаем.
        assertThrows(CloudStorageException.class, () -> {
            fileService.uploadFile(filename, multipartFile, testUser);
        });

        // Также убедимся, что если файл уже существует, то метод save не будет вызван ни разу.
        verify(fileRepository, never()).save(any(File.class));
    }
}


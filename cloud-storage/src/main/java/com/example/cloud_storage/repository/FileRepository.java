package com.example.cloud_storage.repository;


import com.example.cloud_storage.model.File;
import com.example.cloud_storage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    /**
     * Находит файл по имени и владельцу. Используется для проверок, удаления и редактирования.
     */
    Optional<File> findByFilenameAndUser(String filename, User user);

    /**
     * Находит все файлы, принадлежащие конкретному пользователю.
     */
    List<File> findByUser(User user);



}
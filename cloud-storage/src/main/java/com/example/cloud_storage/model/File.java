package com.example.cloud_storage.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "files")
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private Long size; // Размер файла в байтах

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    @Column(nullable = false)
    private String path; // Полный путь к файлу на диске

    @ManyToOne(fetch = FetchType.LAZY) // Много файлов может принадлежать одному пользователю
    @JoinColumn(name = "user_id", nullable = false) // Внешний ключ к таблице users
    private User user;

    @Lob
    @Column(columnDefinition = "BYTEA")
    private byte[] content;
}

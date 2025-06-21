package com.example.cloud_storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {
    private String filename;
    private long size;
    private String uploadDate; // Используйте String для даты, если не хотите передавать LocalDateTime напрямую
}

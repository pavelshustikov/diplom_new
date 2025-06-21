package com.example.cloud_storage.dto;

import com.example.cloud_storage.dto.FileResponse;
import com.example.cloud_storage.model.File;

public class FileMapper {
    public static FileResponse toFileResponse(File file) {
        return new FileResponse(file.getFilename(), file.getSize());
    }
}
package com.rural.marketplace.catalog.service;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import java.nio.file.Path;

public interface FileStorageService {
    String storeFile(MultipartFile file);

    Resource loadFileAsResource(String fileName);

    void deleteFile(String fileName);
}

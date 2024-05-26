package ru.halcyon.meetingease.service.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeImage(MultipartFile file);
}

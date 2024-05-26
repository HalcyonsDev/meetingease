package ru.halcyon.meetingease.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.halcyon.meetingease.service.file.FileStorageService;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {
    private final FileStorageService fileStorageService;

    @PostMapping("/upload-photo")
    public ResponseEntity<String> uploadPhoto(@RequestParam("file") MultipartFile file) {
        String photo = fileStorageService.storeImage(file);
        return ResponseEntity.ok(photo);
    }
}

package ru.halcyon.meetingease.service.file;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.halcyon.meetingease.exception.StorageException;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.service.auth.client.ClientAuthService;
import ru.halcyon.meetingease.service.client.ClientService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    private final ClientService clientService;
    private final ClientAuthService clientAuthService;

    private final Path rootLocation;
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png");

    public FileStorageServiceImpl(
            @Value("${file.storage.location}") String storageLocation,
            @Autowired ClientService clientService,
            @Autowired ClientAuthService clientAuthService
    ) {
        this.rootLocation = Paths.get(storageLocation);
        this.clientService = clientService;
        this.clientAuthService = clientAuthService;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException ex) {
            throw new StorageException("Could not initialize storage.");
        }
    }

    @Override
    public String storeImage(MultipartFile file) {
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new StorageException("Image type should be jpeg/png.");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String filename = UUID.randomUUID() + "-" + originalFilename;

        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }

            Path destinationFile = rootLocation.resolve(filename).normalize().toAbsolutePath();
            saveClientPhoto(destinationFile.toString());

            if (!destinationFile.getParent().equals(rootLocation.toAbsolutePath())) {
                throw new StorageException("Can't store file outside current directory.");
            }

            try (InputStream inputStream = file.getInputStream()) {
               Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return destinationFile.toString();
        } catch (IOException ex) {
            throw new StorageException("Failed to store file.", ex);
        }
    }

    private void saveClientPhoto(String filepath) {
        Client client = clientService.findByEmail(clientAuthService.getAuthInfo().getEmail());
        client.setPhoto(filepath);
        clientService.save(client);
    }
}

package ru.halcyon.meetingease.service.client;

import org.springframework.web.multipart.MultipartFile;
import ru.halcyon.meetingease.dto.ClientUpdateDto;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.support.Role;

public interface ClientService {
    Client save(Client client);
    boolean existsByEmail(String email);
    Client findById(Long clientId);
    Client findByEmail(String email);
    void isVerifiedClient();

    Client uploadPhoto(MultipartFile file);
    Client updateData(ClientUpdateDto dto);
    Client updateRole(Long clientId, Role role);
}

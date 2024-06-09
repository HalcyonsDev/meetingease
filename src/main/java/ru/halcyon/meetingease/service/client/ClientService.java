package ru.halcyon.meetingease.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.halcyon.meetingease.dto.ClientUpdateDto;
import ru.halcyon.meetingease.dto.ResetPasswordRequestDto;
import ru.halcyon.meetingease.exception.ResourceForbiddenException;
import ru.halcyon.meetingease.exception.ResourceNotFoundException;
import ru.halcyon.meetingease.exception.InvalidCredentialsException;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.repository.ClientRepository;
import ru.halcyon.meetingease.security.AuthenticatedDataProvider;
import ru.halcyon.meetingease.security.JwtAuthentication;
import ru.halcyon.meetingease.service.auth.TokenRevocationService;
import ru.halcyon.meetingease.service.file.FileStorageService;
import ru.halcyon.meetingease.support.Role;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;

    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticatedDataProvider authenticatedDataProvider;
    private final TokenRevocationService tokenRevocationService;

    public Client save(Client client) {
        return clientRepository.save(client);
    }

    public boolean existsByEmail(String email) {
        return clientRepository.existsByEmail(email);
    }

    public Client findById(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client with this id not found."));
    }

    public Client findByEmail(String email) {
        return clientRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Client with this email not found."));
    }

    public Client resetPassword(ResetPasswordRequestDto dto) {
        Client client = findByEmail(authenticatedDataProvider.getEmail());

        if (!passwordEncoder.matches(dto.getCurrentPassword(), client.getPassword())) {
            throw new InvalidCredentialsException("Invalid login credentials provided.");
        }

        String encodedNewPassword = passwordEncoder.encode(dto.getNewPassword());
        client.setPassword(encodedNewPassword);

        return save(client);
    }

    public void isVerifiedClient() {
        boolean isClient = getAuthInfo().isClient();
        if (!isClient) {
            throw new ResourceForbiddenException("This feature is not allowed for agents");
        }

        Client client = findByEmail(getAuthInfo().getEmail());

        if (Boolean.FALSE.equals(client.getIsVerified())) {
            throw new ResourceForbiddenException("This feature is not allowed for unverified users. Please confirm your email.");
        }
    }

    public Client uploadPhoto(MultipartFile file) {
        Client client = findByEmail(getAuthInfo().getEmail());
        String filepath = fileStorageService.storeImage(file);
        client.setPhoto(filepath);

        return save(client);
    }

    public Client updateData(ClientUpdateDto dto) {
        Client client = findByEmail(getAuthInfo().getEmail());

        if (!dto.getName().isEmpty()) {
            client.setName(dto.getName());
        }

        if (!dto.getSurname().isEmpty()) {
            client.setSurname(dto.getSurname());
        }

        if (!dto.getEmail().isEmpty()) {
            client.setEmail(dto.getEmail());
            client.setIsVerified(false);
        }

        if (!dto.getPhoneNumber().isEmpty()) {
            client.setPhoto(dto.getPhoneNumber());
        }

        if (!dto.getPosition().isEmpty()) {
            client.setPosition(dto.getPosition());
        }

        return save(client);
    }

    public Client updateRole(Long clientId, Role role) {
        Client client = findByEmail(getAuthInfo().getEmail());
        Client clientToUpdate = findById(clientId);

        isCompanyAdmin(client, clientToUpdate);

        clientToUpdate.setRole(role);
        return save(clientToUpdate);
    }

    public void deactivate() {
        tokenRevocationService.revoke();
    }

    private JwtAuthentication getAuthInfo() {
        return (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
    }

    private void isCompanyAdmin(Client client, Client clientToUpdate) {
        if (client.getRole() == Role.USER || !clientToUpdate.getCompany().equals(client.getCompany())) {
            throw new ResourceForbiddenException("You don't have the rights to update data for this client.");
        }
    }
}

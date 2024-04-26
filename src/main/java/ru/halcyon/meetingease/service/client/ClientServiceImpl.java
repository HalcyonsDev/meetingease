package ru.halcyon.meetingease.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.halcyon.meetingease.exception.ResourceForbiddenException;
import ru.halcyon.meetingease.exception.ResourceNotFoundException;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.repository.ClientRepository;
import ru.halcyon.meetingease.security.JwtAuthentication;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;

    @Override
    public Client save(Client client) {
        return clientRepository.save(client);
    }

    @Override
    public boolean existsByEmail(String email) {
        return clientRepository.existsByEmail(email);
    }

    @Override
    public Client findById(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client with this id not found."));
    }

    @Override
    public Client findByEmail(String email) {
        return clientRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Client with this email not found."));
    }

    @Override
    public void isVerifiedClient() {
        boolean isClient = getAuthInfo().isClient();
        if (!isClient) {
            throw new ResourceForbiddenException("This feature is not allowed for agents");
        }

        Client client = findByEmail(getAuthInfo().getEmail());
        if (!client.getIsVerified()) {
            throw new ResourceForbiddenException("This feature is not allowed for unverified users. Please confirm your email.");
        }
    }

    private JwtAuthentication getAuthInfo() {
        return (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
    }
}

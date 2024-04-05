package ru.halcyon.meetingease.service.client;

import ru.halcyon.meetingease.model.Client;

public interface ClientService {
    Client save(Client client);
    boolean existsByEmail(String email);
    Client findByEmail(String email);
}

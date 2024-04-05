package ru.halcyon.meetingease.service.agent;

import ru.halcyon.meetingease.model.Agent;

public interface AgentService {
    Agent save(Agent agent);
    boolean existsByEmail(String email);
    Agent findByEmail(String email);
}

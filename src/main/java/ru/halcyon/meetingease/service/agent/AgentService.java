package ru.halcyon.meetingease.service.agent;

import ru.halcyon.meetingease.model.Agent;

import java.util.List;

public interface AgentService {
    Agent save(Agent agent);
    boolean existsByEmail(String email);
    Agent findByEmail(String email);
    List<Agent> findAllByCity(String city);
}

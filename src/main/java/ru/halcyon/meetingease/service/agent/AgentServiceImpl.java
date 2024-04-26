package ru.halcyon.meetingease.service.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.halcyon.meetingease.exception.ResourceNotFoundException;
import ru.halcyon.meetingease.model.Agent;
import ru.halcyon.meetingease.repository.AgentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentServiceImpl implements AgentService {
    private final AgentRepository agentRepository;

    @Override
    public Agent save(Agent agent) {
        return agentRepository.save(agent);
    }

    @Override
    public boolean existsByEmail(String email) {
        return agentRepository.existsByEmail(email);
    }

    @Override
    public Agent findById(Long agentId) {
        return agentRepository.findById(agentId)
            .orElseThrow(() -> new ResourceNotFoundException("Agent with this id not found."));   
    }

    @Override
    public Agent findByEmail(String email) {
        return agentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Agent with this email not found."));
    }

    @Override
    public List<Agent> findAllByCity(String city) {
        return agentRepository.findAllByCity(city);
    }
}

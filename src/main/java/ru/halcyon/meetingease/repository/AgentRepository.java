package ru.halcyon.meetingease.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.halcyon.meetingease.model.Agent;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    boolean existsByEmail(String email);
    Optional<Agent> findByEmail(String email);
    List<Agent> findAllByCity(String city);
}

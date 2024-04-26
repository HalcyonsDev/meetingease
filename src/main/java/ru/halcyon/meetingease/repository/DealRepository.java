package ru.halcyon.meetingease.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.halcyon.meetingease.model.Deal;

import java.util.Optional;

@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {
    Optional<Deal> findByType(String type);
}

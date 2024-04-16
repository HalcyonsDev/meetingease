package ru.halcyon.meetingease.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.halcyon.meetingease.model.Client;
import ru.halcyon.meetingease.model.Meeting;
import ru.halcyon.meetingease.model.support.Status;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findAllByStatusAndClientsContaining(Status status, Client client);
}

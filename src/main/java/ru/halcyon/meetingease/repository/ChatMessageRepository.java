package ru.halcyon.meetingease.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.halcyon.meetingease.model.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findAllBySenderIdAndRecipientIdAndIsSenderClient(Long senderId, Long recipientId, Boolean isSenderClient, Pageable pageable);
}

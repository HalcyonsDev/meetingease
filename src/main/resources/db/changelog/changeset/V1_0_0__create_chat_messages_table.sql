-- =========================================
-- Description: Create the chat_messages table
-- Author: Halcyon
-- Date: 2024-04-05
-- Version: V1.0.0
-- =========================================

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    created_at TIMESTAMP NOT NULL,
    content VARCHAR(500) NOT NULL,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    is_sender_client BOOLEAN NOT NULL
)
-- changeset: create-chat-message
-- author: halcyon

-- createTable: chat_messages
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY NOT NULL,
    content VARCHAR(500) NOT NULL,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    is_sender_client BOOLEAN NOT NULL
)
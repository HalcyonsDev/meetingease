-- author: halcyon
-- changeset: create-meetings-clients

-- createTable: meetings_clients
CREATE TABLE IF NOT EXISTS meetings_clients(
    meeting_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    FOREIGN KEY (meeting_id) REFERENCES meetings(id),
    FOREIGN KEY (client_id) REFERENCES clients(id)
)
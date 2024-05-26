-- =========================================
-- Description: Create the meetings_clients table
-- Author: Halcyon
-- Date: 2024-04-05
-- Version: V1.0.0
-- =========================================

CREATE TABLE IF NOT EXISTS meetings_clients(
    meeting_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    FOREIGN KEY (meeting_id) REFERENCES meetings(id),
    FOREIGN KEY (client_id) REFERENCES clients(id)
)
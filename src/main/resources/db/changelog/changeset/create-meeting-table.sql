-- author: halcyon
-- changeset: create-meeting

-- createTable: meetings
CREATE TABLE IF NOT EXISTS meetings(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    date TIMESTAMP NOT NULL,
    address VARCHAR(100) NOT NULL,
    agent_id BIGINT NOT NULL,
    deal_id BIGINT NOT NULL,
    FOREIGN KEY (agent_id) REFERENCES agents(id),
    FOREIGN KEY (deal_id) REFERENCES deals(id)
)
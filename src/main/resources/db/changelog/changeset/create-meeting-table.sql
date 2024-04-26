-- author: halcyon
-- changeset: create-meeting

-- createTable: meetings
CREATE TABLE IF NOT EXISTS meetings(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    date TIMESTAMP NOT NULL,
    status VARCHAR(10) NOT NULL,
    address VARCHAR(500) NOT NULL,
    city VARCHAR(100) NOT NULL,
    street VARCHAR(100) NOT NULL,
    house_number VARCHAR(100) NOT NULL,
    agent_id BIGINT,
    deal_id BIGINT NOT NULL,
    FOREIGN KEY (agent_id) REFERENCES agents(id),
    FOREIGN KEY (deal_id) REFERENCES deals(id)
)
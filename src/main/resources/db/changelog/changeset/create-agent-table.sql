-- changeset: create-agent
-- author: halcyon

-- createTable: agents
CREATE TABLE IF NOT EXISTS agents(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(100) NOT NULL,
    surname VARCHAR(100) NOT NULL,
    email VARCHAR(64) UNIQUE NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    password VARCHAR(100) NOT NULL,
    photo VARCHAR(100)
)
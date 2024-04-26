-- changeset: create-deal
-- author: halcyon

-- createTable: deals
CREATE TABLE IF NOT EXISTS deals(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    type VARCHAR(100) NOT NULL UNIQUE,
    required_documents JSON
)
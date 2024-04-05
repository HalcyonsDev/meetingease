-- author: halcyon
-- changeset: create-document

-- createTable: documents
CREATE TABLE IF NOT EXISTS documents(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(100) NOT NULL,
    is_ready BOOLEAN NOT NULL,
    deal_id BIGINT NOT NULL,
    FOREIGN KEY (deal_id) REFERENCES deals(id)
)
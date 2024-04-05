-- changeset: create-company
-- author: halcyon

-- createTable: companies
CREATE TABLE IF NOT EXISTS companies(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(100),
    description VARCHAR(500)
)
-- changeset: create-client
-- author: halcyon

-- createTable: clients
CREATE TABLE IF NOT EXISTS clients(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(100) NOT NULL,
    surname VARCHAR(100) NOT NULL,
    email VARCHAR(64) UNIQUE NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    position VARCHAR(100) NOT NULL,
    role VARCHAR(5) NOT NULL,
    password VARCHAR(100) NOT NULL,
    photo VARCHAR(100),
    company_id BIGINT,
    FOREIGN KEY (company_id) REFERENCES companies(id)
)
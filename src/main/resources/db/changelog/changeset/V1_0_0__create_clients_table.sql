-- =========================================
-- Description: Create the clients table
-- Author: Halcyon
-- Date: 2024-04-05
-- Version: V1.0.0
-- =========================================

CREATE TABLE IF NOT EXISTS clients(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(100) NOT NULL,
    surname VARCHAR(100) NOT NULL,
    email VARCHAR(64) UNIQUE NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    position VARCHAR(100),
    role VARCHAR(5),
    password VARCHAR(100) NOT NULL,
    photo VARCHAR(100),
    is_verified BOOLEAN NOT NULL,
    company_id BIGINT,
    FOREIGN KEY (company_id) REFERENCES companies(id)
)
-- =========================================
-- Description: Create the agents table
-- Author: Halcyon
-- Date: 2024-05-26
-- Version: V1.0.1
-- =========================================

CREATE TABLE IF NOT EXISTS agents(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(100) NOT NULL,
    surname VARCHAR(100) NOT NULL,
    email VARCHAR(64) UNIQUE NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    city VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    photo VARCHAR(100) NOT NULL
)
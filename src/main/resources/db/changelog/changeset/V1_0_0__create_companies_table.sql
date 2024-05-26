-- =========================================
-- Description: Create the companies table
-- Author: Halcyon
-- Date: 2024-04-05
-- Version: V1.0.0
-- =========================================

CREATE TABLE IF NOT EXISTS companies(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL
)
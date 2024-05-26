-- =========================================
-- Description: Create the deals table
-- Author: Halcyon
-- Date: 2024-04-05
-- Version: V1.0.0
-- =========================================

CREATE TABLE IF NOT EXISTS deals(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    type VARCHAR(100) NOT NULL UNIQUE,
    required_documents JSON
)
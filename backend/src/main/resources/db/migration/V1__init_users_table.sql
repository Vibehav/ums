CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,

                       name VARCHAR(150) NOT NULL,
                       email VARCHAR(254) NOT NULL,
                       primary_mobile VARCHAR(15) NOT NULL,
                       secondary_mobile VARCHAR(15),
                       aadhaar VARCHAR(12),
                       pan VARCHAR(10),

                       date_of_birth DATE NOT NULL,
                       place_of_birth VARCHAR(150),
                       current_address VARCHAR(500),
                       permanent_address VARCHAR(500),

                       created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                       updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                       deleted_at DATETIME(6),
                       version BIGINT NOT NULL DEFAULT 0,

    -- Database-Level Unique Constraints
                       CONSTRAINT uq_users_email UNIQUE (email),
                       CONSTRAINT uq_users_aadhaar UNIQUE (aadhaar),
                       CONSTRAINT uq_users_pan UNIQUE (pan)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
COMMENT='Core users table holding profile and KYC data';


-- 1. Index on soft-delete column.
-- Since your app heavily relies on querying "WHERE deleted_at IS NULL",
-- this prevents full-table scans.
CREATE INDEX idx_users_deleted_at ON users(deleted_at);

-- 2. (Optional but recommended) Index for logins/lookups.
-- If users log in via mobile, you want this indexed.
-- Email, Aadhaar, and PAN are already implicitly indexed by their UNIQUE constraints.
CREATE INDEX idx_users_primary_mobile ON users(primary_mobile);
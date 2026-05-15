--liquibase formatted sql

--changeset kemalthes:8

CREATE TABLE auth_refresh_sessions (
    id UUID NOT NULL,
    user_id UUID NOT NULL,
    refresh_token_hash VARCHAR(128) NOT NULL,
    client_ip VARCHAR(64) NOT NULL,
    user_agent VARCHAR(512),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    rotated_at TIMESTAMP WITH TIME ZONE,
    revoked_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_auth_refresh_sessions PRIMARY KEY (id),
    CONSTRAINT uk_auth_refresh_sessions_hash UNIQUE (refresh_token_hash),
    CONSTRAINT fk_auth_refresh_sessions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_auth_refresh_sessions_user ON auth_refresh_sessions (user_id);

CREATE TABLE auth_verification_codes (
    id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    code_hash VARCHAR(128) NOT NULL,
    purpose VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    consumed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_auth_verification_codes PRIMARY KEY (id),
    CONSTRAINT chk_auth_verification_codes_purpose CHECK (purpose IN ('REGISTRATION', 'PASSWORD_RESET'))
);

CREATE INDEX idx_auth_verification_codes_email_purpose ON auth_verification_codes (email, purpose);

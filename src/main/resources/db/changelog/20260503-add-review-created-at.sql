-- liquibase formatted sql

-- changeset kemalthes:5

ALTER TABLE reviews
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

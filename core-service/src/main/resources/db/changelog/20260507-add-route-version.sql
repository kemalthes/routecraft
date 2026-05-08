-- liquibase formatted sql

-- changeset kemalthes:6

ALTER TABLE tour_routes ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

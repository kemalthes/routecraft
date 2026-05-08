-- liquibase formatted sql

-- changeset kemalthes:4

ALTER TABLE tour_routes ADD COLUMN status VARCHAR(64);

ALTER TABLE tour_routes ADD CONSTRAINT ch_tour_routes_status CHECK ( status IN ('DRAFT', 'PENDING', 'PUBLISHED') )

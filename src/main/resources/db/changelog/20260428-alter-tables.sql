--liquibase formatted sql

--changeset kemalthes:3

ALTER TABLE tour_routes DROP COLUMN IF EXISTS base_price;

ALTER TABLE roles ALTER COLUMN id TYPE BIGINT;

ALTER TABLE user_roles ALTER COLUMN role_id TYPE BIGINT;

ALTER TABLE tour_routes ADD COLUMN geometry TEXT;

ALTER TABLE tour_routes ADD COLUMN image_url TEXT;

--liquibase formatted sql

--changeset kemalthes:2

--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM roles WHERE name = 'ROLE_USER'
INSERT INTO roles (name) VALUES ('USER');

--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM roles WHERE name = 'ROLE_ADMIN'
INSERT INTO roles (name) VALUES ('ADMIN');
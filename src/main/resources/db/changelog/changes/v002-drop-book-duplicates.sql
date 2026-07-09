-- liquibase formatted sql

-- changeset init:2
ALTER TABLE books DROP COLUMN isbn;
ALTER TABLE books DROP COLUMN title;
ALTER TABLE books DROP COLUMN author;

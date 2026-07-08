-- liquibase formatted sql

-- changeset init:1
CREATE TABLE book_editions (
    id BIGSERIAL PRIMARY KEY,
    isbn VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    UNIQUE (isbn, title, author)
);

CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    edition_id BIGINT NOT NULL REFERENCES book_editions(id),
    isbn VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE borrowers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

CREATE TABLE borrow_records (
    id BIGSERIAL PRIMARY KEY,
    borrower_id BIGINT NOT NULL REFERENCES borrowers(id),
    book_id BIGINT NOT NULL REFERENCES books(id),
    status VARCHAR(20) NOT NULL,
    borrowed_at TIMESTAMPTZ NOT NULL,
    returned_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX idx_unique_borrowed_book ON borrow_records(book_id) WHERE status = 'BORROWED';

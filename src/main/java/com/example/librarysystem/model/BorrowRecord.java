package com.example.librarysystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.ZonedDateTime;

@Entity
@Table(name = "borrow_records")
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "borrower_id", nullable = false)
    private Borrower borrower;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowStatus status;

    @Column(nullable = false)
    private ZonedDateTime borrowedAt;

    private ZonedDateTime returnedAt;

    public BorrowRecord() {}

    public BorrowRecord(Borrower borrower, Book book) {
        this.borrower = borrower;
        this.book = book;
        this.status = BorrowStatus.BORROWED;
        this.borrowedAt = ZonedDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Borrower getBorrower() { return borrower; }
    public void setBorrower(Borrower borrower) { this.borrower = borrower; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public BorrowStatus getStatus() { return status; }
    public void setStatus(BorrowStatus status) { this.status = status; }
    public ZonedDateTime getBorrowedAt() { return borrowedAt; }
    public void setBorrowedAt(ZonedDateTime borrowedAt) { this.borrowedAt = borrowedAt; }
    public ZonedDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(ZonedDateTime returnedAt) { this.returnedAt = returnedAt; }
}

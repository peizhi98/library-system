package com.example.librarysystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "edition_id", nullable = false)
    private BookEdition edition;

    @Column(nullable = false)
    private boolean available;

    public Book() {}

    public Book(BookEdition edition) {
        this.edition = edition;
        this.available = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BookEdition getEdition() { return edition; }
    public void setEdition(BookEdition edition) { this.edition = edition; }
    public String getIsbn() { return edition.getIsbn(); }
    public String getTitle() { return edition.getTitle(); }
    public String getAuthor() { return edition.getAuthor(); }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}

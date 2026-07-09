package com.example.librarysystem.repository;

import com.example.librarysystem.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Query(value = "SELECT b FROM Book b LEFT JOIN FETCH b.edition",
           countQuery = "SELECT COUNT(b) FROM Book b")
    Page<Book> findAllWithEdition(Pageable pageable);
}

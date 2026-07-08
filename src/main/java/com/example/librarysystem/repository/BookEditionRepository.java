package com.example.librarysystem.repository;

import com.example.librarysystem.model.BookEdition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookEditionRepository extends JpaRepository<BookEdition, Long> {
    Optional<BookEdition> findByIsbn(String isbn);
}

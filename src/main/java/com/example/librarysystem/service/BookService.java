package com.example.librarysystem.service;

import com.example.librarysystem.dto.BookDTO;
import com.example.librarysystem.dto.PaginatedBookResponse;
import com.example.librarysystem.model.Book;
import com.example.librarysystem.model.BookEdition;
import com.example.librarysystem.repository.BookEditionRepository;
import com.example.librarysystem.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final BookEditionRepository bookEditionRepository;

    public BookService(BookRepository bookRepository, BookEditionRepository bookEditionRepository) {
        this.bookRepository = bookRepository;
        this.bookEditionRepository = bookEditionRepository;
    }

    @Transactional
    public BookDTO registerBook(String title, String author, String isbn) {
        BookEdition edition = bookEditionRepository.findByIsbn(isbn).orElse(null);
        if (edition != null) {
            if (!edition.getTitle().equals(title) || !edition.getAuthor().equals(author)) {
                throw new IllegalArgumentException(
                        "ISBN " + isbn + " already exists with title '" + edition.getTitle()
                                + "' and author '" + edition.getAuthor() + "'");
            }
        } else {
            edition = new BookEdition(isbn, title, author);
            bookEditionRepository.save(edition);
        }

        Book book = new Book(edition);
        bookRepository.save(book);
        return toDTO(book);
    }

    @Transactional(readOnly = true)
    public PaginatedBookResponse getAllBooks(int page, int size) {
        Page<Book> bookPage = bookRepository.findAllWithEdition(
                PageRequest.of(page, size, Sort.by("id"))
        );
        List<BookDTO> content = bookPage.getContent().stream()
                .map(this::toDTO)
                .toList();
        return new PaginatedBookResponse(content, bookPage.getNumber(), bookPage.getSize(),
                bookPage.getTotalElements(), bookPage.getTotalPages());
    }

    private BookDTO toDTO(Book book) {
        return new BookDTO(book.getId(), book.getEdition().getIsbn(), book.getEdition().getTitle(),
                book.getEdition().getAuthor(), book.isAvailable());
    }
}

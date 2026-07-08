package com.example.librarysystem.service;

import com.example.librarysystem.dto.BookDTO;
import com.example.librarysystem.repository.BookEditionRepository;
import com.example.librarysystem.repository.BookRepository;
import com.example.librarysystem.repository.BorrowRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class BookServiceIntegrationTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookEditionRepository bookEditionRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @BeforeEach
    void setUp() {
        borrowRecordRepository.deleteAll();
        bookRepository.deleteAll();
        bookEditionRepository.deleteAll();
    }

    @Test
    void registerBook_withoutPriorEdition_shouldCreateEditionAndBook() {
        BookDTO result = bookService.registerBook("Test-Driven Development", "Kent Beck", "978-3-16-148410-0");

        assertThat(result.getId()).isNotNull();
        assertThat(result.getIsbn()).isEqualTo("978-3-16-148410-0");
        assertThat(result.getTitle()).isEqualTo("Test-Driven Development");
        assertThat(result.getAuthor()).isEqualTo("Kent Beck");
        assertThat(result.isAvailable()).isTrue();

        assertThat(bookEditionRepository.count()).isOne();
        assertThat(bookRepository.count()).isOne();
    }

    @Test
    void registerBook_withExistingEdition_shouldCreateAnotherBookCopy() {
        bookService.registerBook("Clean Architecture", "Robert C. Martin", "978-0-13-468599-1");

        BookDTO result = bookService.registerBook("Clean Architecture", "Robert C. Martin", "978-0-13-468599-1");

        assertThat(result.getId()).isNotNull();
        assertThat(result.getIsbn()).isEqualTo("978-0-13-468599-1");
        assertThat(result.getTitle()).isEqualTo("Clean Architecture");
        assertThat(result.getAuthor()).isEqualTo("Robert C. Martin");
        assertThat(result.isAvailable()).isTrue();

        assertThat(bookEditionRepository.count()).isOne();
        assertThat(bookRepository.count()).isEqualTo(2);
    }

    @Test
    void registerBook_withSameIsbnButDifferentTitle_shouldReject() {
        bookService.registerBook("Domain-Driven Design", "Eric Evans", "978-0-201-63361-0");

        assertThatThrownBy(() ->
                bookService.registerBook("Domain Driven Design", "Eric Evans", "978-0-201-63361-0"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("978-0-201-63361-0");
    }
}

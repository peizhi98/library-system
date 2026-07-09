package com.example.librarysystem.service;

import com.example.librarysystem.model.Book;
import com.example.librarysystem.model.BookEdition;
import com.example.librarysystem.model.BorrowRecord;
import com.example.librarysystem.model.BorrowStatus;
import com.example.librarysystem.model.Borrower;
import com.example.librarysystem.repository.BookEditionRepository;
import com.example.librarysystem.repository.BookRepository;
import com.example.librarysystem.repository.BorrowRecordRepository;
import com.example.librarysystem.repository.BorrowerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class BorrowBookServiceIntegrationTest {

    @Autowired
    private BorrowBookService borrowBookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookEditionRepository bookEditionRepository;

    @Autowired
    private BorrowerRepository borrowerRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    private Borrower savedBorrower;
    private Book savedBook;

    @BeforeEach
    void setUp() {
        borrowRecordRepository.deleteAll();
        bookRepository.deleteAll();
        bookEditionRepository.deleteAll();
        borrowerRepository.deleteAll();

        BookEdition edition = bookEditionRepository.save(new BookEdition("978-1-11-111111-1", "Test Book", "Test Author"));
        savedBorrower = borrowerRepository.save(new Borrower("John", "john@test.com"));
        savedBook = bookRepository.save(new Book(edition));
    }

    @Test
    void borrowBook_shouldMarkBookUnavailableAndCreateRecord() {
        borrowBookService.borrowBook(savedBorrower.getId(), savedBook.getId());

        Book book = bookRepository.findById(savedBook.getId()).orElseThrow();
        assertThat(book.isAvailable()).isFalse();

        BorrowRecord record = borrowRecordRepository
                .findByBorrowerIdAndBookIdAndStatus(savedBorrower.getId(), savedBook.getId(), BorrowStatus.BORROWED)
                .orElseThrow();
        assertThat(record.getBorrower().getId()).isEqualTo(savedBorrower.getId());
        assertThat(record.getBook().getId()).isEqualTo(savedBook.getId());
        assertThat(record.getBorrowedAt()).isNotNull();
    }

    @Test
    void borrowBook_withUnknownBorrower_shouldThrow() {
        assertThatThrownBy(() -> borrowBookService.borrowBook(999L, savedBook.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Borrower not found");
    }

    @Test
    void borrowBook_withUnknownBook_shouldThrow() {
        assertThatThrownBy(() -> borrowBookService.borrowBook(savedBorrower.getId(), 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Book not found");
    }

    @Test
    void borrowBook_whenBookUnavailable_shouldThrow() {
        borrowBookService.borrowBook(savedBorrower.getId(), savedBook.getId());

        assertThatThrownBy(() -> borrowBookService.borrowBook(savedBorrower.getId(), savedBook.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already borrowed");
    }

    @Test
    void returnBook_shouldMarkBookAvailableAndUpdateRecord() {
        borrowBookService.borrowBook(savedBorrower.getId(), savedBook.getId());

        borrowBookService.returnBook(savedBorrower.getId(), savedBook.getId());

        Book book = bookRepository.findById(savedBook.getId()).orElseThrow();
        assertThat(book.isAvailable()).isTrue();

        BorrowRecord record = borrowRecordRepository
                .findByBorrowerIdAndBookIdAndStatus(savedBorrower.getId(), savedBook.getId(), BorrowStatus.RETURNED)
                .orElseThrow();
        assertThat(record.getReturnedAt()).isNotNull();
    }

    @Test
    void returnBook_withUnknownBorrower_shouldThrow() {
        assertThatThrownBy(() -> borrowBookService.returnBook(999L, savedBook.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Borrower not found");
    }

    @Test
    void returnBook_withUnknownBook_shouldThrow() {
        assertThatThrownBy(() -> borrowBookService.returnBook(savedBorrower.getId(), 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Book not found");
    }

    @Test
    void returnBook_withoutPriorBorrow_shouldThrow() {
        assertThatThrownBy(() -> borrowBookService.returnBook(savedBorrower.getId(), savedBook.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("did not borrow this book");
    }
}

package com.example.librarysystem.service;

import com.example.librarysystem.model.Book;
import com.example.librarysystem.model.BorrowRecord;
import com.example.librarysystem.model.BorrowStatus;
import com.example.librarysystem.model.Borrower;
import com.example.librarysystem.repository.BookRepository;
import com.example.librarysystem.repository.BorrowRecordRepository;
import com.example.librarysystem.repository.BorrowerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class BorrowBookService {
    private final BookRepository bookRepository;
    private final BorrowerRepository borrowerRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    public BorrowBookService(BookRepository bookRepository, BorrowerRepository borrowerRepository,
                             BorrowRecordRepository borrowRecordRepository) {
        this.bookRepository = bookRepository;
        this.borrowerRepository = borrowerRepository;
        this.borrowRecordRepository = borrowRecordRepository;
    }

    @Transactional
    public void borrowBook(Long borrowerId, Long bookId) {
        Borrower borrower = borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new IllegalArgumentException("Borrower not found with id: " + borrowerId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + bookId));

        if (!book.isAvailable()) {
            throw new IllegalStateException("Book is already borrowed: " + bookId);
        }

        book.setAvailable(false);
        bookRepository.save(book);

        BorrowRecord record = new BorrowRecord(borrower, book);
        borrowRecordRepository.save(record);
    }

    @Transactional
    public void returnBook(Long borrowerId, Long bookId) {
        Borrower borrower = borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> new IllegalArgumentException("Borrower not found with id: " + borrowerId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + bookId));

        BorrowRecord record = borrowRecordRepository
                .findByBorrowerIdAndBookIdAndStatus(borrowerId, bookId, BorrowStatus.BORROWED)
                .orElseThrow(() -> new IllegalStateException("Borrower did not borrow this book: " + bookId));

        record.setStatus(BorrowStatus.RETURNED);
        record.setReturnedAt(ZonedDateTime.now());
        borrowRecordRepository.save(record);

        book.setAvailable(true);
        bookRepository.save(book);
    }
}

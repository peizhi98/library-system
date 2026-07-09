package com.example.librarysystem.controller;

import com.example.librarysystem.service.BorrowBookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BorrowRecordController {
    private final BorrowBookService borrowBookService;

    public BorrowRecordController(BorrowBookService borrowBookService) {
        this.borrowBookService = borrowBookService;
    }

    @PostMapping("/api/borrowers/{borrowerId}/borrow/{bookId}")
    public ResponseEntity<String> borrowBook(@PathVariable Long borrowerId, @PathVariable Long bookId) {
        borrowBookService.borrowBook(borrowerId, bookId);
        return ResponseEntity.ok("Book borrowed successfully");
    }

    @PostMapping("/api/borrowers/{borrowerId}/return/{bookId}")
    public ResponseEntity<String> returnBook(@PathVariable Long borrowerId, @PathVariable Long bookId) {
        borrowBookService.returnBook(borrowerId, bookId);
        return ResponseEntity.ok("Book returned successfully");
    }
}

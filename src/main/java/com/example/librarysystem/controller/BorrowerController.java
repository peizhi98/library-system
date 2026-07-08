package com.example.librarysystem.controller;

import com.example.librarysystem.dto.BorrowerDTO;
import com.example.librarysystem.dto.RegisterBorrowerRequest;
import com.example.librarysystem.service.BorrowBookService;
import com.example.librarysystem.service.BorrowerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/borrowers")
public class BorrowerController {
    private final BorrowerService borrowerService;
    private final BorrowBookService borrowBookService;

    public BorrowerController(BorrowerService borrowerService, BorrowBookService borrowBookService) {
        this.borrowerService = borrowerService;
        this.borrowBookService = borrowBookService;
    }

    @PostMapping
    public ResponseEntity<BorrowerDTO> registerBorrower(@Valid @RequestBody RegisterBorrowerRequest request) {
        BorrowerDTO borrower = borrowerService.registerBorrower(
                request.getName(),
                request.getEmail()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(borrower);
    }

    @PostMapping("/{borrowerId}/borrow/{bookId}")
    public ResponseEntity<String> borrowBook(@PathVariable Long borrowerId, @PathVariable Long bookId) {
        borrowBookService.borrowBook(borrowerId, bookId);
        return ResponseEntity.ok("Book borrowed successfully");
    }

    @PostMapping("/{borrowerId}/return/{bookId}")
    public ResponseEntity<String> returnBook(@PathVariable Long borrowerId, @PathVariable Long bookId) {
        borrowBookService.returnBook(borrowerId, bookId);
        return ResponseEntity.ok("Book returned successfully");
    }
}

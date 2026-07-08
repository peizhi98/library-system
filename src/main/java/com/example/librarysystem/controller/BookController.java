package com.example.librarysystem.controller;

import com.example.librarysystem.dto.BookDTO;
import com.example.librarysystem.dto.PaginatedBookResponse;
import com.example.librarysystem.dto.RegisterBookRequest;
import com.example.librarysystem.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<BookDTO> registerBook(@Valid @RequestBody RegisterBookRequest request) {
        BookDTO book = bookService.registerBook(
                request.getTitle(),
                request.getAuthor(),
                request.getIsbn()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }

    @GetMapping
    public ResponseEntity<PaginatedBookResponse> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PaginatedBookResponse response = bookService.getAllBooks(page, size);
        return ResponseEntity.ok(response);
    }
}

package com.example.librarysystem.controller;

import com.example.librarysystem.dto.BorrowerDTO;
import com.example.librarysystem.dto.RegisterBorrowerRequest;
import com.example.librarysystem.service.BorrowerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/borrowers")
public class BorrowerController {
    private final BorrowerService borrowerService;

    public BorrowerController(BorrowerService borrowerService) {
        this.borrowerService = borrowerService;
    }

    @PostMapping
    public ResponseEntity<BorrowerDTO> registerBorrower(@Valid @RequestBody RegisterBorrowerRequest request) {
        BorrowerDTO borrower = borrowerService.registerBorrower(
                request.getName(),
                request.getEmail()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(borrower);
    }
}

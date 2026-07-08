package com.example.librarysystem.service;

import com.example.librarysystem.dto.BorrowerDTO;
import com.example.librarysystem.model.Borrower;
import com.example.librarysystem.repository.BorrowerRepository;
import org.springframework.stereotype.Service;

@Service
public class BorrowerService {
    private final BorrowerRepository borrowerRepository;

    public BorrowerService(BorrowerRepository borrowerRepository) {
        this.borrowerRepository = borrowerRepository;
    }

    public BorrowerDTO registerBorrower(String name, String email) {
        Borrower borrower = new Borrower(name, email);
        borrowerRepository.save(borrower);
        return toDTO(borrower);
    }

    private BorrowerDTO toDTO(Borrower borrower) {
        return new BorrowerDTO(borrower.getId(), borrower.getName(), borrower.getEmail());
    }
}

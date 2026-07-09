package com.example.librarysystem.service;

import com.example.librarysystem.dto.BorrowerDTO;
import com.example.librarysystem.repository.BorrowRecordRepository;
import com.example.librarysystem.repository.BorrowerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class BorrowerServiceIntegrationTest {

    @Autowired
    private BorrowerService borrowerService;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    @Autowired
    private BorrowerRepository borrowerRepository;

    @BeforeEach
    void setUp() {
        borrowRecordRepository.deleteAll();
        borrowerRepository.deleteAll();
    }

    @Test
    void registerBorrower_shouldCreateAndReturnDTO() {
        BorrowerDTO result = borrowerService.registerBorrower("Alice Smith", "alice@example.com");

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Alice Smith");
        assertThat(result.getEmail()).isEqualTo("alice@example.com");

        assertThat(borrowerRepository.count()).isOne();
    }
}

package com.example.librarysystem.repository;

import com.example.librarysystem.model.BorrowRecord;
import com.example.librarysystem.model.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    Optional<BorrowRecord> findByBorrowerIdAndBookIdAndStatus(Long borrowerId, Long bookId, BorrowStatus status);
}

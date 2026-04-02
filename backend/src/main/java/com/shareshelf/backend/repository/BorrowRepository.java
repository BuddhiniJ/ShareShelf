package com.shareshelf.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shareshelf.backend.entity.Book;
import com.shareshelf.backend.entity.BorrowRequest;
import com.shareshelf.backend.entity.User;

public interface BorrowRepository extends JpaRepository<BorrowRequest, Long> {

    // All requests made by a borrower
    Page<BorrowRequest> findByBorrower(User borrower, Pageable pageable);

    // All requests for books owned by a specific user
    @Query("""
        SELECT br FROM BorrowRequest br
        JOIN br.book b
        WHERE b.owner = :owner
        """)
    Page<BorrowRequest> findByBookOwner(
        @Param("owner") User owner,
        Pageable pageable
    );

    // All requests for a specific book
    Page<BorrowRequest> findByBook(Book book, Pageable pageable);

    // Check if a pending/approved request already exists for this borrower+book
    // Prevents duplicate borrow requests
    @Query("""
        SELECT br FROM BorrowRequest br
        WHERE br.borrower = :borrower
        AND br.book = :book
        AND br.status IN ('PENDING', 'APPROVED')
        """)
    Optional<BorrowRequest> findActiveBorrowRequest(
        @Param("borrower") User borrower,
        @Param("book") Book book
    );

    // Count how many active borrows a user currently has
    @Query("""
        SELECT COUNT(br) FROM BorrowRequest br
        WHERE br.borrower = :borrower
        AND br.status = 'APPROVED'
        """)
    long countActiveBorrows(@Param("borrower") User borrower);
}

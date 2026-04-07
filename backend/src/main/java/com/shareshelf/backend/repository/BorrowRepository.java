package com.shareshelf.backend.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shareshelf.backend.entity.Book;
import com.shareshelf.backend.entity.BorrowRequest;
import com.shareshelf.backend.entity.BorrowStatus;
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
            AND br.status IN :statuses
            """)
        Optional<BorrowRequest> findActiveBorrowRequest(
            @Param("borrower") User borrower,
            @Param("book") Book book,
            @Param("statuses") List<BorrowStatus> statuses
        );

    // Count how many active borrows a user currently has
    @Query("""
            SELECT COUNT(br) FROM BorrowRequest br
            WHERE br.borrower = :borrower
            AND br.status = :status
            """)
        long countActiveBorrows(
            @Param("borrower") User borrower,
            @Param("status") BorrowStatus status
        );
    
 // Find a RETURNED borrow for a specific borrower and book
    @Query("""
        SELECT br FROM BorrowRequest br
        WHERE br.id = :id
        AND br.borrower = :borrower
        AND br.status = com.shareshelf.backend.entity.BorrowStatus.RETURNED
        """)
    Optional<BorrowRequest> findReturnedBorrowByIdAndBorrower(
        @Param("id") Long id,
        @Param("borrower") User borrower, BorrowStatus returned
    );
    
 // Count by status — for stats
    long countByStatus(BorrowStatus status);

    // Total borrows made by a user
    long countByBorrower(User borrower);

    // Total borrow requests received for a user's books
    @Query("""
        SELECT COUNT(br) FROM BorrowRequest br
        WHERE br.book.owner = :owner
        """)
    long countByBookOwner(@Param("owner") User owner);
}

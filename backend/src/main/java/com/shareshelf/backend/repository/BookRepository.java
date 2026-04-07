package com.shareshelf.backend.repository;

import com.shareshelf.backend.entity.Book;
import com.shareshelf.backend.entity.Book.BookStatus;
import com.shareshelf.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    // ✅ findByStatus → matches Java field name 'status'
    Page<Book> findByStatus(BookStatus status, Pageable pageable);

    Page<Book> findByOwner(User owner, Pageable pageable);

    // ✅ b.status → Java field name in JPQL, not DB column name
    @Query("""
        SELECT b FROM Book b
        WHERE b.status = :status
        AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<Book> searchByKeyword(
        @Param("keyword") String keyword,
        @Param("status") BookStatus status,
        Pageable pageable
    );

    // ✅ findByGenreIgnoreCaseAndStatus → 'status' matches Java field
    Page<Book> findByGenreIgnoreCaseAndStatus(
        String genre,
        BookStatus status,
        Pageable pageable
    );
    
 // Count by status — for stats
    long countByStatus(BookStatus status);

    // Admin: all books regardless of status, with owner filter option
    Page<Book> findAll(Pageable pageable);

	long countByOwner(User user);
}
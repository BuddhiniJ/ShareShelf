package com.shareshelf.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shareshelf.backend.entity.Book;
import com.shareshelf.backend.entity.BorrowRequest;
import com.shareshelf.backend.entity.Review;
import com.shareshelf.backend.entity.User;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // All reviews for a specific book
    Page<Review> findByBook(Book book, Pageable pageable);

    // All reviews written by a specific user
    Page<Review> findByReviewer(User reviewer, Pageable pageable);

    // Check if a review already exists for this borrow request
    boolean existsByBorrowRequest(BorrowRequest borrowRequest);

    // Find review by borrow request (for duplicate check with detail)
    Optional<Review> findByBorrowRequest(BorrowRequest borrowRequest);

    // Average rating for a book
    @Query("""
        SELECT COALESCE(AVG(r.rating), 0.0)
        FROM Review r
        WHERE r.book = :book
        """)
    Double getAverageRatingByBook(@Param("book") Book book);

    // Count of reviews for a book
    long countByBook(Book book);

    // All reviews for books owned by a specific user (owner sees feedback)
    @Query("""
        SELECT r FROM Review r
        JOIN r.book b
        WHERE b.owner = :owner
        """)
    Page<Review> findReviewsForOwnerBooks(
        @Param("owner") User owner,
        Pageable pageable
    );
    
 // Count reviews written by a user
    long countByReviewer(User reviewer);

    // Platform-wide average rating
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r")
    double getPlatformAverageRating();
}
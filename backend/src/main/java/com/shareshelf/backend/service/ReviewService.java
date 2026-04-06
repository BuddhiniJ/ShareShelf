package com.shareshelf.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shareshelf.backend.dto.ReviewRequest;
import com.shareshelf.backend.dto.ReviewResponse;
import com.shareshelf.backend.entity.Book;
import com.shareshelf.backend.entity.BorrowRequest;
import com.shareshelf.backend.entity.BorrowStatus;
import com.shareshelf.backend.entity.Review;
import com.shareshelf.backend.entity.User;
import com.shareshelf.backend.exception.ResourceNotFoundException;
import com.shareshelf.backend.exception.UnauthorizedException;
import com.shareshelf.backend.repository.BookRepository;
import com.shareshelf.backend.repository.BorrowRepository;
import com.shareshelf.backend.repository.PagedResponse;
import com.shareshelf.backend.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final BorrowRepository borrowRepository;
    private final UserService userService;

    // ── Submit a review ───────────────────────────────────────────────────

    public ReviewResponse submitReview(Long bookId, ReviewRequest request) {
        User reviewer = userService.getCurrentUser();

        // Verify book exists
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        // Cannot review your own book
        if (book.getOwner().getId().equals(reviewer.getId())) {
            throw new UnauthorizedException("You cannot review your own book");
        }

        // Verify borrow request exists, belongs to reviewer, and is RETURNED
        BorrowRequest borrowRequest = borrowRepository
        	    .findReturnedBorrowByIdAndBorrower(
        	        request.getBorrowRequestId(),
        	        reviewer,
        	        BorrowStatus.RETURNED                               // ✅ pass as parameter
        	    )
        	    .orElseThrow(() -> new IllegalStateException(
        	        "You can only review a book after you have returned it"
        	    ));

        // Verify the borrow is for this specific book
        if (!borrowRequest.getBook().getId().equals(bookId)) {
            throw new IllegalStateException(
                "This borrow request is not for the specified book"
            );
        }

        // One review per borrow — no duplicates
        if (reviewRepository.existsByBorrowRequest(borrowRequest)) {
            throw new IllegalStateException(
                "You have already reviewed this borrow"
            );
        }

        Review review = Review.builder()
                .reviewer(reviewer)
                .book(book)
                .borrowRequest(borrowRequest)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        return mapToResponse(reviewRepository.save(review));
    }

    // ── Get all reviews for a book ────────────────────────────────────────

    public PagedResponse<ReviewResponse> getReviewsForBook(
            Long bookId, int page, int size) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        Pageable pageable = PageRequest.of(
            page, size, Sort.by("createdAt").descending()
        );
        Page<Review> reviews = reviewRepository.findByBook(book, pageable);
        return buildPagedResponse(reviews);
    }

    // ── Get reviews written by the current user ───────────────────────────

    public PagedResponse<ReviewResponse> getMyReviews(int page, int size) {
        User reviewer = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(
            page, size, Sort.by("createdAt").descending()
        );
        Page<Review> reviews = reviewRepository.findByReviewer(reviewer, pageable);
        return buildPagedResponse(reviews);
    }

    // ── Get reviews for books the current user owns ───────────────────────

    public PagedResponse<ReviewResponse> getReviewsForMyBooks(int page, int size) {
        User owner = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(
            page, size, Sort.by("createdAt").descending()
        );
        Page<Review> reviews = reviewRepository
            .findReviewsForOwnerBooks(owner, pageable);
        return buildPagedResponse(reviews);
    }

    // ── Delete a review (reviewer only) ──────────────────────────────────

    public void deleteReview(Long reviewId) {
        User currentUser = userService.getCurrentUser();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Review", "id", reviewId)
                );

        if (!review.getReviewer().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .reviewerId(review.getReviewer().getId())
                .reviewerName(review.getReviewer().getName())
                .bookId(review.getBook().getId())
                .bookTitle(review.getBook().getTitle())
                .borrowRequestId(review.getBorrowRequest().getId())
                .build();
    }

    private PagedResponse<ReviewResponse> buildPagedResponse(Page<Review> page) {
        List<ReviewResponse> content = page.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return PagedResponse.<ReviewResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}

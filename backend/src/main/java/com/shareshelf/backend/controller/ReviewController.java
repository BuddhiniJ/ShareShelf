package com.shareshelf.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shareshelf.backend.dto.ReviewRequest;
import com.shareshelf.backend.dto.ReviewResponse;
import com.shareshelf.backend.repository.PagedResponse;
import com.shareshelf.backend.service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // POST /api/reviews/{bookId} — submit a review for a book
    @PostMapping("/{bookId}")
    public ResponseEntity<ReviewResponse> submitReview(
            @PathVariable Long bookId,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.submitReview(bookId, request));
    }

    // GET /api/reviews/{bookId} — get all reviews for a book (public)
    @GetMapping("/{bookId}")
    public ResponseEntity<PagedResponse<ReviewResponse>> getReviewsForBook(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getReviewsForBook(bookId, page, size));
    }

    // GET /api/reviews/my-reviews — reviews written by current user
    @GetMapping("/my-reviews")
    public ResponseEntity<PagedResponse<ReviewResponse>> getMyReviews(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getMyReviews(page, size));
    }

    // GET /api/reviews/my-books — reviews on books I own
    @GetMapping("/my-books")
    public ResponseEntity<PagedResponse<ReviewResponse>> getReviewsForMyBooks(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getReviewsForMyBooks(page, size));
    }

    // DELETE /api/reviews/{reviewId} — delete own review
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}

package com.shareshelf.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shareshelf.backend.dto.BorrowActionRequest;
import com.shareshelf.backend.dto.BorrowRequestResponse;
import com.shareshelf.backend.repository.PagedResponse;
import com.shareshelf.backend.service.BorrowService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
public class BorrowController {
	
	private final BorrowService borrowService;

    // POST /api/borrow/{bookId} — request to borrow a book
    @PostMapping("/{bookId}")
    public ResponseEntity<BorrowRequestResponse> requestBorrow(
            @PathVariable Long bookId,
            @RequestParam(required = false) String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(borrowService.requestBorrow(bookId, message));
    }

    // GET /api/borrow/my-requests — borrower views their own requests
    @GetMapping("/my-requests")
    public ResponseEntity<PagedResponse<BorrowRequestResponse>> getMyRequests(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(borrowService.getMyBorrowRequests(page, size));
    }

    // GET /api/borrow/incoming — owner views requests for their books
    @GetMapping("/incoming")
    public ResponseEntity<PagedResponse<BorrowRequestResponse>> getIncomingRequests(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(borrowService.getIncomingRequests(page, size));
    }

    // GET /api/borrow/{requestId} — view a specific request
    @GetMapping("/{requestId}")
    public ResponseEntity<BorrowRequestResponse> getRequestById(
            @PathVariable Long requestId) {
        return ResponseEntity.ok(borrowService.getRequestById(requestId));
    }

    // PUT /api/borrow/{requestId}/approve — owner approves
    @PutMapping("/{requestId}/approve")
    public ResponseEntity<BorrowRequestResponse> approveRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody BorrowActionRequest action) {
        return ResponseEntity.ok(borrowService.approveRequest(requestId, action));
    }

    // PUT /api/borrow/{requestId}/reject — owner rejects
    @PutMapping("/{requestId}/reject")
    public ResponseEntity<BorrowRequestResponse> rejectRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody BorrowActionRequest action) {
        return ResponseEntity.ok(borrowService.rejectRequest(requestId, action));
    }

    // PUT /api/borrow/{requestId}/cancel — borrower cancels their own request
    @PutMapping("/{requestId}/cancel")
    public ResponseEntity<BorrowRequestResponse> cancelRequest(
            @PathVariable Long requestId) {
        return ResponseEntity.ok(borrowService.cancelRequest(requestId));
    }

    // PUT /api/borrow/{requestId}/return — owner marks book as returned
    @PutMapping("/{requestId}/return")
    public ResponseEntity<BorrowRequestResponse> markAsReturned(
            @PathVariable Long requestId) {
        return ResponseEntity.ok(borrowService.markAsReturned(requestId));
    }

}

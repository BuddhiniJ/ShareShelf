package com.shareshelf.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shareshelf.backend.dto.BorrowActionRequest;
import com.shareshelf.backend.dto.BorrowRequestResponse;
import com.shareshelf.backend.entity.Book;
import com.shareshelf.backend.entity.Book.BookStatus;
import com.shareshelf.backend.entity.BorrowRequest;
import com.shareshelf.backend.entity.BorrowStatus;
import com.shareshelf.backend.entity.User;
import com.shareshelf.backend.exception.ResourceNotFoundException;
import com.shareshelf.backend.exception.UnauthorizedException;
import com.shareshelf.backend.repository.BookRepository;
import com.shareshelf.backend.repository.BorrowRepository;
import com.shareshelf.backend.repository.PagedResponse;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowRepository borrowRepository;
    private final BookRepository bookRepository;
    private final UserService userService;

    // ── Request to borrow a book ──────────────────────────────────────────

    public BorrowRequestResponse requestBorrow(Long bookId, String message) {
        User borrower = userService.getCurrentUser();

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        // Cannot borrow your own book
        if (book.getOwner().getId().equals(borrower.getId())) {
            throw new UnauthorizedException("You cannot borrow your own book");
        }

        // Book must be available
        if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new IllegalStateException("This book is not available for borrowing");
        }

        // No duplicate active requests
        borrowRepository.findActiveBorrowRequest(
        	    borrower,
        	    book,
        	    List.of(BorrowStatus.PENDING, BorrowStatus.APPROVED)   // ✅ pass as parameter
        	).ifPresent(existing -> {
        	    throw new IllegalStateException(
        	        "You already have an active borrow request for this book"
        	    );
        	});

        BorrowRequest request = BorrowRequest.builder()
                .borrower(borrower)
                .book(book)
                .message(message)
                .status(BorrowStatus.PENDING)
                .build();

        return mapToResponse(borrowRepository.save(request));
    }

    // ── Borrower: view their own requests ─────────────────────────────────

    public PagedResponse<BorrowRequestResponse> getMyBorrowRequests(int page, int size) {
        User borrower = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("requestedAt").descending());
        Page<BorrowRequest> requests = borrowRepository.findByBorrower(borrower, pageable);
        return buildPagedResponse(requests);
    }

    // ── Owner: view incoming requests for their books ─────────────────────

    public PagedResponse<BorrowRequestResponse> getIncomingRequests(int page, int size) {
        User owner = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("requestedAt").descending());
        Page<BorrowRequest> requests = borrowRepository.findByBookOwner(owner, pageable);
        return buildPagedResponse(requests);
    }

    // ── Owner: approve a request ──────────────────────────────────────────

    public BorrowRequestResponse approveRequest(Long requestId, BorrowActionRequest action) {
        User owner = userService.getCurrentUser();
        BorrowRequest borrowRequest = findRequestOrThrow(requestId);

        // Only the book owner can approve
        if (!borrowRequest.getBook().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You are not the owner of this book");
        }

        // Can only approve PENDING requests
        if (borrowRequest.getStatus() != BorrowStatus.PENDING) {
            throw new IllegalStateException(
                "Only PENDING requests can be approved. Current status: "
                + borrowRequest.getStatus()
            );
        }

        // Update request
        borrowRequest.setStatus(BorrowStatus.APPROVED);
        borrowRequest.setOwnerNote(action.getOwnerNote());
        borrowRequest.setDueDate(action.getDueDate());

        // Auto-update book status → BORROWED
        borrowRequest.getBook().setStatus(BookStatus.BORROWED);
        bookRepository.save(borrowRequest.getBook());

        return mapToResponse(borrowRepository.save(borrowRequest));
    }

    // ── Owner: reject a request ───────────────────────────────────────────

    public BorrowRequestResponse rejectRequest(Long requestId, BorrowActionRequest action) {
        User owner = userService.getCurrentUser();
        BorrowRequest borrowRequest = findRequestOrThrow(requestId);

        if (!borrowRequest.getBook().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You are not the owner of this book");
        }

        if (borrowRequest.getStatus() != BorrowStatus.PENDING) {
            throw new IllegalStateException(
                "Only PENDING requests can be rejected. Current status: "
                + borrowRequest.getStatus()
            );
        }

        borrowRequest.setStatus(BorrowStatus.REJECTED);
        borrowRequest.setOwnerNote(action.getOwnerNote());

        return mapToResponse(borrowRepository.save(borrowRequest));
    }

    // ── Borrower: cancel their own pending request ────────────────────────

    public BorrowRequestResponse cancelRequest(Long requestId) {
        User borrower = userService.getCurrentUser();
        BorrowRequest borrowRequest = findRequestOrThrow(requestId);

        if (!borrowRequest.getBorrower().getId().equals(borrower.getId())) {
            throw new UnauthorizedException("You did not make this borrow request");
        }

        if (borrowRequest.getStatus() != BorrowStatus.PENDING) {
            throw new IllegalStateException(
                "Only PENDING requests can be cancelled. Current status: "
                + borrowRequest.getStatus()
            );
        }

        borrowRequest.setStatus(BorrowStatus.CANCELLED);
        return mapToResponse(borrowRepository.save(borrowRequest));
    }

    // ── Owner: mark book as returned ─────────────────────────────────────

    public BorrowRequestResponse markAsReturned(Long requestId) {
        User owner = userService.getCurrentUser();
        BorrowRequest borrowRequest = findRequestOrThrow(requestId);

        if (!borrowRequest.getBook().getOwner().getId().equals(owner.getId())) {
            throw new UnauthorizedException("You are not the owner of this book");
        }

        if (borrowRequest.getStatus() != BorrowStatus.APPROVED) {
            throw new IllegalStateException(
                "Only APPROVED borrows can be marked as returned. Current status: "
                + borrowRequest.getStatus()
            );
        }

        // Update request
        borrowRequest.setStatus(BorrowStatus.RETURNED);

        // Auto-update book status → AVAILABLE again
        borrowRequest.getBook().setStatus(BookStatus.AVAILABLE);
        bookRepository.save(borrowRequest.getBook());

        return mapToResponse(borrowRepository.save(borrowRequest));
    }

    // ── Get single request by ID ──────────────────────────────────────────

    public BorrowRequestResponse getRequestById(Long requestId) {
        User currentUser = userService.getCurrentUser();
        BorrowRequest borrowRequest = findRequestOrThrow(requestId);

        boolean isBorrower = borrowRequest.getBorrower().getId().equals(currentUser.getId());
        boolean isOwner = borrowRequest.getBook().getOwner().getId().equals(currentUser.getId());

        if (!isBorrower && !isOwner) {
            throw new UnauthorizedException("You are not involved in this borrow request");
        }

        return mapToResponse(borrowRequest);
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    private BorrowRequest findRequestOrThrow(Long id) {
        return borrowRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException("BorrowRequest", "id", id)
                );
    }

    private BorrowRequestResponse mapToResponse(BorrowRequest br) {
        return BorrowRequestResponse.builder()
                .id(br.getId())
                .status(br.getStatus())
                .message(br.getMessage())
                .ownerNote(br.getOwnerNote())
                .requestedAt(br.getRequestedAt())
                .updatedAt(br.getUpdatedAt())
                .dueDate(br.getDueDate())
                // Book info
                .bookId(br.getBook().getId())
                .bookTitle(br.getBook().getTitle())
                .bookAuthor(br.getBook().getAuthor())
                // Borrower info
                .borrowerId(br.getBorrower().getId())
                .borrowerName(br.getBorrower().getName())
                // Owner info
                .ownerId(br.getBook().getOwner().getId())
                .ownerName(br.getBook().getOwner().getName())
                .build();
    }

    private PagedResponse<BorrowRequestResponse> buildPagedResponse(
            Page<BorrowRequest> page) {

        List<BorrowRequestResponse> content = page.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return PagedResponse.<BorrowRequestResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
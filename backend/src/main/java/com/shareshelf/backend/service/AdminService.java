package com.shareshelf.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shareshelf.backend.dto.AdminUserResponse;
import com.shareshelf.backend.dto.BookResponse;
import com.shareshelf.backend.dto.PlatformStatsResponse;
import com.shareshelf.backend.dto.UpdateRoleRequest;
import com.shareshelf.backend.entity.Book;
import com.shareshelf.backend.entity.BorrowStatus;
import com.shareshelf.backend.entity.User;
import com.shareshelf.backend.entity.User.Role;
import com.shareshelf.backend.exception.ResourceNotFoundException;
import com.shareshelf.backend.exception.UnauthorizedException;
import com.shareshelf.backend.repository.BookRepository;
import com.shareshelf.backend.repository.BorrowRepository;
import com.shareshelf.backend.repository.NotificationRepository;
import com.shareshelf.backend.repository.PagedResponse;
import com.shareshelf.backend.repository.ReviewRepository;
import com.shareshelf.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowRepository borrowRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;
    private final UserService userService;

    // ── User Management ───────────────────────────────────────────────────

    public PagedResponse<AdminUserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(
            page, size, Sort.by("fullName").ascending()
        );
        Page<User> users = userRepository.findAll(pageable);
        return buildUserPagedResponse(users);
    }

    public PagedResponse<AdminUserResponse> searchUsers(
            String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(
            page, size, Sort.by("fullName").ascending()
        );
        Page<User> users = userRepository.searchUsers(keyword, pageable);
        return buildUserPagedResponse(users);
    }

    public AdminUserResponse getUserDetails(Long userId) {
        User user = findUserOrThrow(userId);
        return mapToAdminUserResponse(user);
    }

    public AdminUserResponse updateUserRole(Long userId, UpdateRoleRequest request) {
        User currentAdmin = userService.getCurrentUser();

        // Prevent admin from demoting themselves
        if (currentAdmin.getId().equals(userId)
                && request.getRole() == Role.USER) {
            throw new UnauthorizedException(
                "You cannot remove your own admin privileges"
            );
        }

        User user = findUserOrThrow(userId);
        user.setRole(request.getRole());
        return mapToAdminUserResponse(userRepository.save(user));
    }

    public void deleteUser(Long userId) {
        User currentAdmin = userService.getCurrentUser();

        if (currentAdmin.getId().equals(userId)) {
            throw new UnauthorizedException("You cannot delete your own account");
        }

        User user = findUserOrThrow(userId);
        userRepository.delete(user);
    }

    // ── Book Management ───────────────────────────────────────────────────

    public PagedResponse<BookResponse> getAllBooks(int page, int size) {
        Pageable pageable = PageRequest.of(
            page, size, Sort.by("createdAt").descending()
        );
        Page<Book> books = bookRepository.findAll(pageable);
        return buildBookPagedResponse(books);
    }

    public void forceDeleteBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Book", "id", bookId)
                );
        bookRepository.delete(book);
    }

    // ── Platform Statistics ───────────────────────────────────────────────

    public PlatformStatsResponse getPlatformStats() {
        return PlatformStatsResponse.builder()
                // Users
                .totalUsers(userRepository.count())
                .totalAdmins(userRepository.countByRole(Role.ADMIN))
                // Books
                .totalBooks(bookRepository.count())
                .availableBooks(
                    bookRepository.countByStatus(Book.BookStatus.AVAILABLE)
                )
                .borrowedBooks(
                    bookRepository.countByStatus(Book.BookStatus.BORROWED)
                )
                // Borrows
                .totalBorrowRequests(borrowRepository.count())
                .pendingRequests(
                    borrowRepository.countByStatus(BorrowStatus.PENDING)
                )
                .approvedRequests(
                    borrowRepository.countByStatus(BorrowStatus.APPROVED)
                )
                .returnedRequests(
                    borrowRepository.countByStatus(BorrowStatus.RETURNED)
                )
                .rejectedRequests(
                    borrowRepository.countByStatus(BorrowStatus.REJECTED)
                )
                // Reviews
                .totalReviews(reviewRepository.count())
                .averageRatingPlatform(
                    reviewRepository.getPlatformAverageRating()
                )
                // Notifications
                .totalUnreadNotifications(
                    notificationRepository.countAllUnread()
                )
                .build();
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                    new ResourceNotFoundException("User", "id", userId)
                );
    }

    private AdminUserResponse mapToAdminUserResponse(User user) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .fullName(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .bio(user.getBio())
                .profilePicture(user.getProfilePicture())
                .totalBooksListed(bookRepository.countByOwner(user))
                .totalBorrowsMade(borrowRepository.countByBorrower(user))
                .totalBorrowsReceived(borrowRepository.countByBookOwner(user))
                .totalReviewsWritten(reviewRepository.countByReviewer(user))
                .build();
    }

    private BookResponse mapToBookResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .description(book.getDescription())
                .coverImage(book.getCoverImage())
                .book_condition(book.getCondition())
                .book_status(book.getStatus())
                .genre(book.getGenre())
                .ownerId(book.getOwner().getId())
                .ownerName(book.getOwner().getName())
                .averageRating(reviewRepository.getAverageRatingByBook(book))
                .reviewCount(reviewRepository.countByBook(book))
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    private PagedResponse<AdminUserResponse> buildUserPagedResponse(
            Page<User> page) {

        List<AdminUserResponse> content = page.getContent()
                .stream()
                .map(this::mapToAdminUserResponse)
                .toList();

        return PagedResponse.<AdminUserResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private PagedResponse<BookResponse> buildBookPagedResponse(Page<Book> page) {
        List<BookResponse> content = page.getContent()
                .stream()
                .map(this::mapToBookResponse)
                .toList();

        return PagedResponse.<BookResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
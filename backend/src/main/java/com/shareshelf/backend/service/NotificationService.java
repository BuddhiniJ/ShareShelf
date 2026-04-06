package com.shareshelf.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shareshelf.backend.dto.NotificationResponse;
import com.shareshelf.backend.entity.Notification;
import com.shareshelf.backend.entity.NotificationType;
import com.shareshelf.backend.entity.User;
import com.shareshelf.backend.exception.ResourceNotFoundException;
import com.shareshelf.backend.exception.UnauthorizedException;
import com.shareshelf.backend.repository.NotificationRepository;
import com.shareshelf.backend.repository.PagedResponse;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    // ── Called by NotificationEventListener only ──────────────────────────

    public void createNotification(
            User recipient,
            NotificationType type,
            String title,
            String message,
            Long bookId,
            Long borrowRequestId) {

        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .message(message)
                .bookId(bookId)
                .borrowRequestId(borrowRequestId)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    // ── Get all notifications for current user ────────────────────────────

    public PagedResponse<NotificationResponse> getMyNotifications(
            int page, int size) {

        User currentUser = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(
            page, size, Sort.by("createdAt").descending()
        );
        Page<Notification> notifications =
            notificationRepository.findByRecipient(currentUser, pageable);
        return buildPagedResponse(notifications);
    }

    // ── Get only unread notifications ─────────────────────────────────────

    public PagedResponse<NotificationResponse> getUnreadNotifications(
            int page, int size) {

        User currentUser = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(
            page, size, Sort.by("createdAt").descending()
        );
        Page<Notification> notifications =
            notificationRepository.findByRecipientAndIsRead(
                currentUser, false, pageable
            );
        return buildPagedResponse(notifications);
    }

    // ── Get unread count (for frontend badge) ─────────────────────────────

    public long getUnreadCount() {
        User currentUser = userService.getCurrentUser();
        return notificationRepository.countByRecipientAndIsRead(currentUser, false);
    }

    // ── Mark single notification as read ─────────────────────────────────

    public NotificationResponse markAsRead(Long notificationId) {
        User currentUser = userService.getCurrentUser();

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Notification", "id", notificationId)
                );

        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException(
                "You cannot mark another user's notification as read"
            );
        }

        notification.setIsRead(true);
        return mapToResponse(notificationRepository.save(notification));
    }

    // ── Mark all notifications as read ────────────────────────────────────

    public void markAllAsRead() {
        User currentUser = userService.getCurrentUser();
        notificationRepository.markAllAsReadForUser(currentUser);
    }

    // ── Delete a notification ─────────────────────────────────────────────

    public void deleteNotification(Long notificationId) {
        User currentUser = userService.getCurrentUser();

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Notification", "id", notificationId)
                );

        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException(
                "You cannot delete another user's notification"
            );
        }

        notificationRepository.delete(notification);
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .isRead(n.getIsRead())
                .bookId(n.getBookId())
                .borrowRequestId(n.getBorrowRequestId())
                .createdAt(n.getCreatedAt())
                .build();
    }

    private PagedResponse<NotificationResponse> buildPagedResponse(
            Page<Notification> page) {

        List<NotificationResponse> content = page.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return PagedResponse.<NotificationResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}

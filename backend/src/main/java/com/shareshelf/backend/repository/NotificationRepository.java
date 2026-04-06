package com.shareshelf.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shareshelf.backend.entity.Notification;
import com.shareshelf.backend.entity.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // All notifications for a user (read + unread)
    Page<Notification> findByRecipient(User recipient, Pageable pageable);

    // Unread notifications only
    Page<Notification> findByRecipientAndIsRead(
        User recipient,
        Boolean isRead,
        Pageable pageable
    );

    // Count of unread — for notification badge in frontend
    long countByRecipientAndIsRead(User recipient, Boolean isRead);

    // Mark all as read for a user in one query
    @Modifying
    @Query("""
        UPDATE Notification n
        SET n.isRead = true
        WHERE n.recipient = :recipient
        AND n.isRead = false
        """)
    void markAllAsReadForUser(@Param("recipient") User recipient);
}
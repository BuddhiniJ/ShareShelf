package com.shareshelf.backend.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.shareshelf.backend.entity.BorrowRequest;
import com.shareshelf.backend.entity.NotificationType;
import com.shareshelf.backend.entity.Review;
import com.shareshelf.backend.event.BorrowApprovedEvent;
import com.shareshelf.backend.event.BorrowCancelledEvent;
import com.shareshelf.backend.event.BorrowRejectedEvent;
import com.shareshelf.backend.event.BorrowRequestedEvent;
import com.shareshelf.backend.event.BorrowReturnedEvent;
import com.shareshelf.backend.event.ReviewPostedEvent;
import com.shareshelf.backend.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    // ── Borrow requested → notify book owner ─────────────────────────────

    @Async
    @EventListener
    public void handleBorrowRequested(BorrowRequestedEvent event) {
        BorrowRequest br = event.getBorrowRequest();

        notificationService.createNotification(
            br.getBook().getOwner(),
            NotificationType.BORROW_REQUESTED,
            "New Borrow Request",
            br.getBorrower().getName()
                + " has requested to borrow \"" + br.getBook().getTitle() + "\"",
            br.getBook().getId(),
            br.getId()
        );
    }

    // ── Borrow approved → notify borrower ────────────────────────────────

    @Async
    @EventListener
    public void handleBorrowApproved(BorrowApprovedEvent event) {
        BorrowRequest br = event.getBorrowRequest();

        notificationService.createNotification(
            br.getBorrower(),
            NotificationType.BORROW_APPROVED,
            "Borrow Request Approved",
            "Your request to borrow \"" + br.getBook().getTitle()
                + "\" has been approved!",
            br.getBook().getId(),
            br.getId()
        );
    }

    // ── Borrow rejected → notify borrower ────────────────────────────────

    @Async
    @EventListener
    public void handleBorrowRejected(BorrowRejectedEvent event) {
        BorrowRequest br = event.getBorrowRequest();

        notificationService.createNotification(
            br.getBorrower(),
            NotificationType.BORROW_REJECTED,
            "Borrow Request Rejected",
            "Your request to borrow \"" + br.getBook().getTitle()
                + "\" was not approved.",
            br.getBook().getId(),
            br.getId()
        );
    }

    // ── Borrow cancelled → notify book owner ─────────────────────────────

    @Async
    @EventListener
    public void handleBorrowCancelled(BorrowCancelledEvent event) {
        BorrowRequest br = event.getBorrowRequest();

        notificationService.createNotification(
            br.getBook().getOwner(),
            NotificationType.BORROW_CANCELLED,
            "Borrow Request Cancelled",
            br.getBorrower().getName()
                + " cancelled their request for \"" + br.getBook().getTitle() + "\"",
            br.getBook().getId(),
            br.getId()
        );
    }

    // ── Book returned → notify owner ─────────────────────────────────────

    @Async
    @EventListener
    public void handleBorrowReturned(BorrowReturnedEvent event) {
        BorrowRequest br = event.getBorrowRequest();

        notificationService.createNotification(
            br.getBook().getOwner(),
            NotificationType.BOOK_RETURNED,
            "Book Returned",
            br.getBorrower().getName()
                + " has returned \"" + br.getBook().getTitle() + "\"",
            br.getBook().getId(),
            br.getId()
        );
    }

    // ── Review posted → notify book owner ────────────────────────────────

    @Async
    @EventListener
    public void handleReviewPosted(ReviewPostedEvent event) {
        Review review = event.getReview();

        notificationService.createNotification(
            review.getBook().getOwner(),
            NotificationType.REVIEW_POSTED,
            "New Review on Your Book",
            review.getReviewer().getName()
                + " left a " + review.getRating()
                + "-star review on \"" + review.getBook().getTitle() + "\"",
            review.getBook().getId(),
            null
        );
    }
}

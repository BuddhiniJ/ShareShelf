package com.shareshelf.backend.entity;

public enum NotificationType {
	BORROW_REQUESTED,       // owner gets this
    BORROW_APPROVED,        // borrower gets this
    BORROW_REJECTED,        // borrower gets this
    BORROW_CANCELLED,       // owner gets this
    BOOK_RETURNED,          // owner gets this
    REVIEW_POSTED           // owner gets this

}

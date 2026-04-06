package com.shareshelf.backend.dto;

import java.time.LocalDateTime;

import com.shareshelf.backend.entity.NotificationType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private Boolean isRead;
    private Long bookId;
    private Long borrowRequestId;
    private LocalDateTime createdAt;
}
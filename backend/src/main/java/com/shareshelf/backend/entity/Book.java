package com.shareshelf.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(unique = true)
    private String isbn;

    @Column(length = 1000)
    private String description;

    private String coverImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "book_condition", nullable = false)   // ✅ DB column = book_condition
    private BookCondition condition;                     // ✅ Java field = condition

    @Enumerated(EnumType.STRING)
    @Column(name = "book_status", nullable = false)      // ✅ DB column = book_status
    @Builder.Default
    private BookStatus status = BookStatus.AVAILABLE;    // ✅ Java field = status

    @Column(nullable = false)
    private String genre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ── Enums (or move to separate files) ────────────────────────────────

    public enum BookStatus {
        AVAILABLE,
        BORROWED,
        UNAVAILABLE
    }

    public enum BookCondition {
        NEW,
        LIKE_NEW,
        GOOD,
        FAIR,
        POOR
    }
}
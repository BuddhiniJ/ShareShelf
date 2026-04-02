package com.shareshelf.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shareshelf.backend.dto.BookRequest;
import com.shareshelf.backend.dto.BookResponse;
import com.shareshelf.backend.repository.PagedResponse;
import com.shareshelf.backend.service.BookService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // POST /api/books — list a new book
    @PostMapping
    public ResponseEntity<BookResponse> createBook(
            @Valid @RequestBody BookRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookService.createBook(request));
    }

    // GET /api/books — browse all available books
    @GetMapping
    public ResponseEntity<PagedResponse<BookResponse>> getAllBooks(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookService.getAllAvailableBooks(page, size));
    }

    // GET /api/books/search?keyword=hobbit — search by title or author
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<BookResponse>> searchBooks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookService.searchBooks(keyword, page, size));
    }

    // GET /api/books/genre?name=fiction — filter by genre
    @GetMapping("/genre")
    public ResponseEntity<PagedResponse<BookResponse>> getByGenre(
            @RequestParam String name,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookService.getBooksByGenre(name, page, size));
    }

    // GET /api/books/me — current user's own listings
    @GetMapping("/me")
    public ResponseEntity<PagedResponse<BookResponse>> getMyBooks(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookService.getMyBooks(page, size));
    }

    // GET /api/books/{id} — single book detail
    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    // PUT /api/books/{id} — update own listing
    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request) {
        return ResponseEntity.ok(bookService.updateBook(id, request));
    }

    // DELETE /api/books/{id} — delete own listing
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();   // 204 No Content
    }
}
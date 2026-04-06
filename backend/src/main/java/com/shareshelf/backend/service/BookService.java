package com.shareshelf.backend.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shareshelf.backend.dto.BookRequest;
import com.shareshelf.backend.dto.BookResponse;
import com.shareshelf.backend.entity.Book;
import com.shareshelf.backend.entity.Book.BookStatus;
import com.shareshelf.backend.entity.User;
import com.shareshelf.backend.exception.ResourceNotFoundException;
import com.shareshelf.backend.exception.UnauthorizedException;
import com.shareshelf.backend.repository.BookRepository;
import com.shareshelf.backend.repository.PagedResponse;
import com.shareshelf.backend.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BookService {

	private final BookRepository bookRepository;
	private final UserService userService; 
	private final ReviewRepository reviewRepository;

	// ── Create a new book listing ─────────────────────────────────────────

	public BookResponse createBook(BookRequest request) {
		User currentUser = userService.getCurrentUser();

		Book book = Book.builder().title(request.getTitle()).author(request.getAuthor()).isbn(request.getIsbn())
				.description(request.getDescription()).coverImage(request.getCoverImage())
				.condition(request.getCondition()) // ✅ 'condition' not 'book_condition'
				.genre(request.getGenre()).status(BookStatus.AVAILABLE) // ✅ 'status' not 'book_status'
				.owner(currentUser).build();

		return mapToResponse(bookRepository.save(book));
	}

	// ── Browse all available books (paginated) ────────────────────────────

	public PagedResponse<BookResponse> getAllAvailableBooks(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Page<Book> books = bookRepository.findByStatus(BookStatus.AVAILABLE, pageable);
		return buildPagedResponse(books);
	}

	// ── Search books by keyword ───────────────────────────────────────────

	public PagedResponse<BookResponse> searchBooks(String keyword, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Page<Book> books = bookRepository.searchByKeyword(keyword, BookStatus.AVAILABLE, pageable);
		return buildPagedResponse(books);
	}

	// ── Filter books by genre ─────────────────────────────────────────────

	public PagedResponse<BookResponse> getBooksByGenre(String genre, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Page<Book> books = bookRepository.findByGenreIgnoreCaseAndStatus(genre, BookStatus.AVAILABLE, pageable);
		return buildPagedResponse(books);
	}

	// ── Get single book by ID ─────────────────────────────────────────────

	public BookResponse getBookById(Long id) {
		Book book = findBookOrThrow(id);
		return mapToResponse(book);
	}

	// ── Get current user's own listings ───────────────────────────────────

	public PagedResponse<BookResponse> getMyBooks(int page, int size) {
		User currentUser = userService.getCurrentUser();
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Page<Book> books = bookRepository.findByOwner(currentUser, pageable);
		return buildPagedResponse(books);
	}

	// ── Update a book listing (owner only) ────────────────────────────────

	public BookResponse updateBook(Long id, BookRequest request) {
		Book book = findBookOrThrow(id);
		checkOwnership(book);

		book.setTitle(request.getTitle());
		book.setAuthor(request.getAuthor());
		book.setIsbn(request.getIsbn());
		book.setDescription(request.getDescription());
		book.setCoverImage(request.getCoverImage());
		book.setCondition(request.getCondition());
		book.setGenre(request.getGenre());

		return mapToResponse(bookRepository.save(book));
	}

	// ── Delete a book listing (owner only) ────────────────────────────────

	public void deleteBook(Long id) {
		Book book = findBookOrThrow(id);
		checkOwnership(book);
		bookRepository.delete(book);
	}

	// ── Internal helpers ──────────────────────────────────────────────────

	private Book findBookOrThrow(Long id) {
		return bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
	}

	// Ownership guard — reused by update and delete
	private void checkOwnership(Book book) {
		User currentUser = userService.getCurrentUser();
		if (!book.getOwner().getId().equals(currentUser.getId())) {
			throw new UnauthorizedException("You are not authorized to modify this book listing");
		}
	}

	// Entity → DTO
	private BookResponse mapToResponse(Book book) {
		Double avgRating = reviewRepository.getAverageRatingByBook(book);
	    Long reviewCount = reviewRepository.countByBook(book);
	    
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
	            .averageRating(avgRating)           // ✅ new
	            .reviewCount(reviewCount)           // ✅ new
	            .createdAt(book.getCreatedAt())
	            .updatedAt(book.getUpdatedAt())
	            .build();
	}

	// Page<Book> → PagedResponse<BookResponse>
	private PagedResponse<BookResponse> buildPagedResponse(Page<Book> page) {
		List<BookResponse> content = page.getContent().stream().map(this::mapToResponse).toList();

		return PagedResponse.<BookResponse>builder().content(content).page(page.getNumber()).size(page.getSize())
				.totalElements(page.getTotalElements()).totalPages(page.getTotalPages()).last(page.isLast()).build();
	}
}
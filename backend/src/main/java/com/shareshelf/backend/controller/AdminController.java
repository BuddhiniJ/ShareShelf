package com.shareshelf.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shareshelf.backend.dto.AdminUserResponse;
import com.shareshelf.backend.dto.BookResponse;
import com.shareshelf.backend.dto.PlatformStatsResponse;
import com.shareshelf.backend.dto.UpdateRoleRequest;
import com.shareshelf.backend.repository.PagedResponse;
import com.shareshelf.backend.service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")     // ✅ locks entire controller
public class AdminController {

    private final AdminService adminService;

    // ── User Management ───────────────────────────────────────────────────

    // GET /api/admin/users — list all users
    @GetMapping("/users")
    public ResponseEntity<PagedResponse<AdminUserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllUsers(page, size));
    }

    // GET /api/admin/users/search?keyword=john — search users
    @GetMapping("/users/search")
    public ResponseEntity<PagedResponse<AdminUserResponse>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.searchUsers(keyword, page, size));
    }

    // GET /api/admin/users/{id} — get user detail with stats
    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserResponse> getUserDetails(
            @PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserDetails(id));
    }

    // PUT /api/admin/users/{id}/role — promote or demote a user
    @PutMapping("/users/{id}/role")
    public ResponseEntity<AdminUserResponse> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(adminService.updateUserRole(id, request));
    }

    // DELETE /api/admin/users/{id} — delete a user
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ── Book Management ───────────────────────────────────────────────────

    // GET /api/admin/books — all books regardless of status
    @GetMapping("/books")
    public ResponseEntity<PagedResponse<BookResponse>> getAllBooks(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getAllBooks(page, size));
    }

    // DELETE /api/admin/books/{id} — force remove any book
    @DeleteMapping("/books/{id}")
    public ResponseEntity<Void> forceDeleteBook(@PathVariable Long id) {
        adminService.forceDeleteBook(id);
        return ResponseEntity.noContent().build();
    }

    // ── Platform Statistics ───────────────────────────────────────────────

    // GET /api/admin/stats — platform-wide numbers
    @GetMapping("/stats")
    public ResponseEntity<PlatformStatsResponse> getPlatformStats() {
        return ResponseEntity.ok(adminService.getPlatformStats());
    }
}
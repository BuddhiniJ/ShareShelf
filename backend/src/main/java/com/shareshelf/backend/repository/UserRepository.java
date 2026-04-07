package com.shareshelf.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shareshelf.backend.entity.User;
import com.shareshelf.backend.entity.User.Role;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findById(Long id);
    
 // Count by role
    long countByRole(Role role);

    // Find all users with pagination — admin browse
    Page<User> findAll(Pageable pageable);

    // Search users by name or email
    @Query("""
        SELECT u FROM User u
        WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
}
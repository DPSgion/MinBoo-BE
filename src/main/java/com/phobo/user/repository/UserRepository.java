package com.phobo.user.repository;

import com.phobo.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM users u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(u.name) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :term, '%'))")
    Page<User> searchUsers(@Param("term") String term, Pageable pageable);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
    boolean existsByPhoneAndIdNot(String phone, UUID id);

}

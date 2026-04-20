package com.phobo.reaction.repository;

import com.phobo.reaction.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    // Tìm cảm xúc của 1 user trên 1 bài viết
    Optional<Reaction> findByPostIdAndUserId(UUID postId, UUID userId);
}
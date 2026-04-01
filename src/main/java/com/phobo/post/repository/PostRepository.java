package com.phobo.post.repository;

import com.phobo.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    // Lọc bài viết Feed (Bài của mình + Bài của bạn bè mang quyền public/friends)
    @Query(value = "SELECT p.* FROM posts p " +
            "WHERE p.deleted_at IS NULL AND " +
            "(p.user_id = :userId OR " +
            "  (p.user_id IN (" +
            "    SELECT user_id_b FROM friends WHERE user_id_a = :userId UNION " +
            "    SELECT user_id_a FROM friends WHERE user_id_b = :userId" +
            "  ) AND p.privacy IN ('public', 'friends'))" +
            ") " +
            "ORDER BY p.created_at DESC",
            countQuery = "SELECT count(*) FROM posts p WHERE p.deleted_at IS NULL AND (p.user_id = :userId OR (p.user_id IN (SELECT user_id_b FROM friends WHERE user_id_a = :userId UNION SELECT user_id_a FROM friends WHERE user_id_b = :userId) AND p.privacy IN ('public', 'friends')))",
            nativeQuery = true)
    Page<Post> getFeedPosts(@Param("userId") UUID userId, Pageable pageable);

    // Đếm số comment
    @Query(value = "SELECT COUNT(*) FROM comments WHERE post_id = :postId", nativeQuery = true)
    long countCommentsByPostId(@Param("postId") UUID postId);

    // Đếm từng loại cảm xúc
    @Query(value = "SELECT type, COUNT(*) FROM reactions WHERE post_id = :postId GROUP BY type", nativeQuery = true)
    List<Object[]> countReactionsByPostId(@Param("postId") UUID postId);

    // Xem User hiện tại đã thả cảm xúc gì chưa
    @Query(value = "SELECT type FROM reactions WHERE post_id = :postId AND user_id = :userId LIMIT 1", nativeQuery = true)
    String getMyReaction(@Param("postId") UUID postId, @Param("userId") UUID userId);
}

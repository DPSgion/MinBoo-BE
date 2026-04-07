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
            "WHERE p.deleted_at IS NULL AND (" +
            "   p.user_id = :userId " +                       // 1. Lấy bài của chính mình
            "   OR p.privacy = 'public' " +                   // 2. Lấy TẤT CẢ bài public của mọi người (kể cả người lạ)
            "   OR (p.privacy = 'friends' AND p.user_id IN (" + // 3. Lấy bài 'friends' của bạn bè
            "       SELECT user_id_b FROM friends WHERE user_id_a = :userId UNION " +
            "       SELECT user_id_a FROM friends WHERE user_id_b = :userId" +
            "   ))" +
            ") " +
            "ORDER BY RANDOM()", //  lấy ngẫu nhiên

            // Nhớ phải sửa cả câu countQuery cho khớp với logic WHERE ở trên nhé
            countQuery = "SELECT count(*) FROM posts p WHERE p.deleted_at IS NULL AND (p.user_id = :userId OR p.privacy = 'public' OR (p.privacy = 'friends' AND p.user_id IN (SELECT user_id_b FROM friends WHERE user_id_a = :userId UNION SELECT user_id_a FROM friends WHERE user_id_b = :userId)))",
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


    // Lấy bài viết trên trang cá nhân của một User
    @Query(value = "SELECT p.* FROM posts p " +
            "WHERE p.deleted_at IS NULL " +
            "AND p.user_id = :profileOwnerId " +
            "AND (" +
            "  :viewerId = :profileOwnerId " + // Trường hợp 1: Tự xem trang của chính mình (Thấy tất cả)
            "  OR p.privacy = 'public' " +     // Trường hợp 2: Bài viết công khai (Ai cũng thấy)
            "  OR (p.privacy = 'friends' AND EXISTS (" + // Trường hợp 3: Bài dành cho bạn bè
            "      SELECT 1 FROM friends f WHERE " +
            "      (f.user_id_a = :viewerId AND f.user_id_b = :profileOwnerId) OR " +
            "      (f.user_id_a = :profileOwnerId AND f.user_id_b = :viewerId)" +
            "  ))" +
            ") ORDER BY p.created_at DESC",
            countQuery = "SELECT count(*) FROM posts p WHERE p.deleted_at IS NULL AND p.user_id = :profileOwnerId AND (:viewerId = :profileOwnerId OR p.privacy = 'public' OR (p.privacy = 'friends' AND EXISTS (SELECT 1 FROM friends f WHERE (f.user_id_a = :viewerId AND f.user_id_b = :profileOwnerId) OR (f.user_id_a = :profileOwnerId AND f.user_id_b = :viewerId))))",
            nativeQuery = true)
    Page<Post> getUserProfilePosts(@Param("viewerId") UUID viewerId, @Param("profileOwnerId") UUID profileOwnerId, Pageable pageable);
}

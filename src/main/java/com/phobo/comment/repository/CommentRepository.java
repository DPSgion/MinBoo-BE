package com.phobo.comment.repository;

import com.phobo.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Tự động phân trang bình luận của một bài viết, sắp xếp mới nhất lên đầu
    Page<Comment> findByPostIdOrderByCreatedAtDesc(UUID postId, Pageable pageable);
}

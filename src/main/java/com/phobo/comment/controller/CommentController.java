package com.phobo.comment.controller;

import com.phobo.comment.dto.CommentDto;
import com.phobo.comment.dto.CommentRequest;
import com.phobo.comment.entity.Comment;
import com.phobo.comment.mapper.CommentMapper;
import com.phobo.comment.repository.CommentRepository;
import com.phobo.comment.service.CommentService;
import com.phobo.common.Moderation.ContentModerationService;
import com.phobo.common.exception.BusinessException;
import com.phobo.post.repository.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/posts/{post_id}/comments")
public class CommentController {

    private final CommentService commentService;
    private final PostRepository postRepository;
    private final ContentModerationService contentModerationService;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;

    public CommentController(CommentService commentService, PostRepository postRepository, ContentModerationService contentModerationService, CommentMapper commentMapper, CommentRepository commentRepository) {
        this.commentService = commentService;
        this.postRepository = postRepository;
        this.contentModerationService = contentModerationService;
        this.commentMapper = commentMapper;
        this.commentRepository = commentRepository;
    }

    //Lấy danh sách bình luận
    @GetMapping
    public ResponseEntity<Map<String, Object>> getComments(
            @PathVariable("post_id") UUID postId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {

        Map<String, Object> data = commentService.getComments(postId, page, limit);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    //Thêm cmt
    @PostMapping
    public ResponseEntity<Map<String, Object>> createComment(
            @RequestHeader("user-id") UUID userId,
            @PathVariable("post_id") UUID postId,
            @RequestBody CommentRequest request) {

        CommentDto newComment = commentService.createComment(postId, userId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", newComment);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //Xóa cmt
    @DeleteMapping("/{comment_id}")
    public ResponseEntity<Map<String, Object>> deleteComment(
            @RequestHeader("user-id") UUID userId,
            @PathVariable("post_id") UUID postId,
            @PathVariable("comment_id") Long commentId) {

        commentService.deleteComment(postId, commentId, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Comment deleted successfully");

        return ResponseEntity.ok(response);
    }
}
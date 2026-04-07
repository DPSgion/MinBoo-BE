package com.phobo.comment.controller;

import com.phobo.comment.dto.CommentDto;
import com.phobo.comment.dto.CommentRequest;
import com.phobo.comment.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/posts/{post_id}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
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
}
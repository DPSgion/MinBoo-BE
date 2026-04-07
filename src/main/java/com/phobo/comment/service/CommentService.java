package com.phobo.comment.service;

import com.phobo.comment.dto.CommentDto;
import com.phobo.comment.dto.CommentRequest;

import java.util.Map;
import java.util.UUID;

public interface CommentService {
    Map<String, Object> getComments(UUID postId, int page, int limit);
    CommentDto createComment(UUID postId, UUID userId, CommentRequest request);
}

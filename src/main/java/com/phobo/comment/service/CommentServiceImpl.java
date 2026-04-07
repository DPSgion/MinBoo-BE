package com.phobo.comment.service;

import com.phobo.comment.dto.CommentDto;
import com.phobo.comment.dto.CommentRequest;
import com.phobo.comment.entity.Comment;
import com.phobo.comment.mapper.CommentMapper;
import com.phobo.comment.repository.CommentRepository;
import com.phobo.common.Moderation.ContentModerationService;
import com.phobo.common.exception.BusinessException;
import com.phobo.post.entity.Post;
import com.phobo.post.repository.PostRepository;
import com.phobo.user.entity.User;
import com.phobo.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ContentModerationService contentModerationService;
    private final CommentMapper commentMapper;

    public CommentServiceImpl(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository, ContentModerationService contentModerationService, CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.contentModerationService = contentModerationService;
        this.commentMapper = commentMapper;
    }

    @Override
    public Map<String, Object> getComments(UUID postId, int page, int limit) {
        postRepository.findById(postId).orElseThrow(() -> new BusinessException(404, "POST_NOT_FOUND"));

        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Comment> commentPage = commentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);

        // THUẬT TOÁN ĐƯỢC RÚT GỌN CHỈ CÒN 1 DÒNG:
        List<CommentDto> dtoList = commentPage.getContent().stream()
                .map(commentMapper::toDto) // Gọi Mapper ở đây
                .collect(Collectors.toList());

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put("total", commentPage.getTotalElements());

        Map<String, Object> data = new HashMap<>();
        data.put("comments", dtoList);
        data.put("pagination", pagination);

        return data;
    }
}

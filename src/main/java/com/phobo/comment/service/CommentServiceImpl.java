package com.phobo.comment.service;

import com.phobo.comment.dto.CommentDto;
import com.phobo.comment.dto.CommentRequest;
import com.phobo.comment.entity.Comment;
import com.phobo.comment.mapper.CommentMapper;
import com.phobo.comment.repository.CommentRepository;
import com.phobo.common.Moderation.ContentModerationService;
import com.phobo.common.exception.BusinessException;
import com.phobo.notification.entity.NotificationType;
import com.phobo.notification.service.NotificationService;
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
    private final NotificationService notificationService;

    public CommentServiceImpl(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository, ContentModerationService contentModerationService, CommentMapper commentMapper, NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.contentModerationService = contentModerationService;
        this.commentMapper = commentMapper;
        this.notificationService = notificationService;
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

    @Override
    @Transactional
    public CommentDto createComment(UUID postId, UUID userId, CommentRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty() || request.getContent().length() > 1000) {
            throw new BusinessException(400, "BAD_REQUEST_INVALID_CONTENT");
        }

        Post post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(404, "POST_NOT_FOUND"));
        contentModerationService.moderateText(request.getContent());

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(request.getContent().trim());
        Comment savedComment = commentRepository.save(comment);

        // [THÊM LOGIC THÔNG BÁO]
        // Chỉ gửi thông báo nếu người comment KHÔNG PHẢI là chủ bài viết
        if (!post.getUserId().equals(userId)) {
            User sender = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND"));
            User recipient = userRepository.findById(post.getUserId())
                    .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND"));

            // Cắt ngắn nội dung comment nếu quá dài để hiển thị trên thông báo cho đẹp
            String shortContent = request.getContent().trim();
            if (shortContent.length() > 30) {
                shortContent = shortContent.substring(0, 30) + "...";
            }

            notificationService.createNotification(
                    recipient,                      // Chủ bài viết
                    sender,                         // Người comment
                    NotificationType.new_comment,   // Enum: new_comment
                    sender.getName() + " đã bình luận về bài viết của bạn: \"" + shortContent + "\"",
                    postId.toString(),              // Target ID để FE bấm vào nhảy đến đúng bài viết
                    "post"                          // Entity Type khớp với DB constraint
            );
        }

        // DÙNG MAPPER ĐỂ TRẢ VỀ:
        return commentMapper.toDto(savedComment);
    }

    //Xóa cmt
    @Override
    @Transactional
    public void deleteComment(UUID postId, Long commentId, UUID userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(404, "POST_NOT_FOUND"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new BusinessException(404, "COMMENT_NOT_FOUND"));

        if (!comment.getPostId().equals(postId)) {
            throw new BusinessException(400, "COMMENT_DOES_NOT_BELONG_TO_POST");
        }

        boolean isCommentAuthor = comment.getUserId().equals(userId);
        boolean isPostAuthor = post.getUserId().equals(userId);

        if (!isCommentAuthor && !isPostAuthor) {
            throw new BusinessException(403, "FORBIDDEN");
        }

        commentRepository.delete(comment);
    }

}

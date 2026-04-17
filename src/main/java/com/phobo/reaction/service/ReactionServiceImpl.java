package com.phobo.reaction.service;

import com.phobo.common.exception.BusinessException;
import com.phobo.post.repository.PostRepository;
import com.phobo.reaction.dto.ReactionRequest;
import com.phobo.reaction.dto.ReactionResponse;
import com.phobo.reaction.entity.Reaction;
import com.phobo.reaction.repository.ReactionRepository;
import com.phobo.user.entity.User;
import com.phobo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor // Lombok tự động tạo Constructor cho các final fields
public class ReactionServiceImpl implements ReactionService {

    private final ReactionRepository reactionRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReactionResponse toggleReaction(UUID postId, String username, ReactionRequest request) {
        // 1. Kiểm tra Bài viết và lấy User
        postRepository.findById(postId).orElseThrow(() -> new BusinessException(404, "POST_NOT_FOUND"));
        User user = userRepository.findByUsername(username).orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND"));
        UUID userId = user.getId();

        // 2. Tìm xem người này đã thả cảm xúc vào bài này chưa
        Optional<Reaction> existingReaction = reactionRepository.findByPostIdAndUserId(postId, userId);

        String requestedType = request.getType().toLowerCase();

        if (existingReaction.isPresent()) {
            Reaction reaction = existingReaction.get();

            // Nếu thả trùng cảm xúc cũ -> Hiểu là HỦY (Unlike)
            if (reaction.getType().equals(requestedType)) {
                reactionRepository.delete(reaction);
                return ReactionResponse.builder().action("REMOVED").postId(postId).type(null).build();
            }
            // Nếu thả cảm xúc khác -> Hiểu là CẬP NHẬT (Đổi từ Like sang Love)
            else {
                reaction.setType(requestedType);
                reactionRepository.save(reaction);
                return ReactionResponse.builder().action("UPDATED").postId(postId).type(requestedType).build();
            }
        }
        // Nếu chưa từng thả -> THÊM MỚI
        else {
            Reaction newReaction = new Reaction();
            newReaction.setPostId(postId);
            newReaction.setUserId(userId);
            newReaction.setType(requestedType);
            reactionRepository.save(newReaction);

            return ReactionResponse.builder().action("ADDED").postId(postId).type(requestedType).build();
        }
    }
}
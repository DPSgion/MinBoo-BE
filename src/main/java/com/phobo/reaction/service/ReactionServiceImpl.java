package com.phobo.reaction.service;

import com.phobo.common.exception.BusinessException;
import com.phobo.notification.entity.NotificationType;
import com.phobo.notification.service.NotificationService;
import com.phobo.post.entity.Post;
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
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ReactionResponse toggleReaction(UUID postId, String username, ReactionRequest request) {
        // 1. Kiểm tra Bài viết và lấy User
        Post post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(404, "POST_NOT_FOUND"));
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

                // [THÊM LOGIC THÔNG BÁO]
                sendReactionNotification(post, user, requestedType);

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

            sendReactionNotification(post, user, requestedType);

            return ReactionResponse.builder().action("ADDED").postId(postId).type(requestedType).build();
        }
    }

    // --- HÀM HELPER: Gom logic gửi thông báo cho sạch code ---
    private void sendReactionNotification(Post post, User sender, String reactionType) {
        // Tránh tình trạng tự "thủ dâm tinh thần" (Tự like bài mình không báo)
        if (!post.getUserId().equals(sender.getId())) {
            User recipient = userRepository.findById(post.getUserId())
                    .orElseThrow(() -> new BusinessException(404, "USER_NOT_FOUND"));

            // Biến hóa tiếng Anh sang tiếng Việt có kèm Emoji
            String typeVN = switch (reactionType) {
                case "like" -> "Thích \uD83D\uDC4D";
                case "love" -> "Yêu thích ❤️";
                case "haha" -> "Haha \uD83D\uDE06";
                case "wow" -> "Wow \uD83D\uDE2E";
                case "sad" -> "Buồn \uD83D\uDE22";
                case "angry" -> "Phẫn nộ \uD83D\uDE21";
                default -> "bày tỏ cảm xúc";
            };

            notificationService.createNotification(
                    recipient,
                    sender,
                    NotificationType.new_reaction, // Enum: new_reaction (Đã có trong DB của bạn)
                    sender.getName() + " đã thả " + typeVN + " vào bài viết của bạn.",
                    post.getPostId().toString(),
                    "post" // Entity Type: post
            );
        }
    }
}
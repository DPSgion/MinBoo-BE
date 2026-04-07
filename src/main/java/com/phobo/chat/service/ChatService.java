package com.phobo.chat.service;

import com.phobo.chat.dto.ConversationResponse;
import com.phobo.chat.dto.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ChatService {
    // API 9.5: Tạo cuộc hội thoại mới (hoặc lấy phòng cũ nếu đã từng chat)
    ConversationResponse createOrGetConversation(UUID currentUserId, UUID targetUserId);

    // API 9.1: Lấy danh sách các cuộc hội thoại của User
    List<ConversationResponse> getConversations(UUID currentUserId);

    // API 9.2: Lấy lịch sử tin nhắn của 1 phòng chat (có phân trang)
    public Slice<MessageResponse> getMessages(UUID conversationId, UUID beforeMessageId, int limit);

    // API 9.3: Gửi tin nhắn
    MessageResponse sendMessage(UUID conversationId, UUID senderId, String content, MultipartFile image);

    // API 9.4: Đánh dấu đã xem
    void markAsSeen(UUID conversationId, UUID currentUserId);
}

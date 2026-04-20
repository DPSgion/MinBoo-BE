package com.phobo.chat.service;

import com.phobo.chat.dto.ChatUserDto;
import com.phobo.chat.dto.ConversationResponse;
import com.phobo.chat.dto.MessageResponse;
import com.phobo.chat.entity.Conversation;
import com.phobo.chat.entity.Message;
import com.phobo.chat.repository.ConversationRepository;
import com.phobo.chat.repository.MessageRepository;
import com.phobo.common.exception.BusinessException;
import com.phobo.common.oci.PrivateChatStorageService;
import com.phobo.friends.repository.FriendRepository;
import com.phobo.user.entity.User;
import com.phobo.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {
    @Autowired
    private ConversationRepository conversationRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FriendRepository friendRepository;
    @Autowired
    private PrivateChatStorageService privateChatStorageService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    //1 - create/get phong chat
    @Transactional
    public ConversationResponse createOrGetConversation(UUID currentUserId, UUID targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Không thể tự chat với chính mình!");
        }

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy User hiện tại"));
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy đối tác"));

        // 1. BỨC TƯỜNG LỬA: DÙNG HÀM CÓ SẴN CỦA BẠN ĐỂ KIỂM TRA BẠN BÈ
        boolean isFriend = friendRepository.existsFriendshipBetween(currentUserId, targetUserId);
        if (!isFriend) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Chỉ có thể nhắn tin với người đã là bạn bè!");
        }

        // 2. TẠO HOẶC LẤY PHÒNG CHAT
        // Logic sắp xếp ID để dùng cho hàm findBySortedUsers của Conversation
        UUID id1 = currentUserId.compareTo(targetUserId) < 0 ? currentUserId : targetUserId;
        UUID id2 = currentUserId.compareTo(targetUserId) < 0 ? targetUserId : currentUserId;

        Optional<Conversation> existingConv = conversationRepository.findBySortedUsers(id1, id2);

        Conversation conversation;
        if (existingConv.isPresent()) {
            conversation = existingConv.get();
        } else {
            conversation = new Conversation();
            // Gán đúng thứ tự ID nhỏ trước, lớn sau
            conversation.setUserOne(currentUserId.compareTo(targetUserId) < 0 ? currentUser : targetUser);
            conversation.setUserTwo(currentUserId.compareTo(targetUserId) < 0 ? targetUser : currentUser);
            conversation.setUnreadCount(0);
            conversation = conversationRepository.save(conversation);
        }

        return mapToConversationResponse(conversation, currentUserId);
    }

    //2 - get danh sach phong chat
    public List<ConversationResponse> getConversations(UUID currentUserId) {
        List<Conversation> conversations = conversationRepository.findAllByUserId(currentUserId);

        return conversations.stream()
                .map(conv -> mapToConversationResponse(conv, currentUserId))
                .collect(Collectors.toList());
    }

    //3 - get lich su tin nhan
    @Override
    public Slice<MessageResponse> getMessages(UUID conversationId, UUID beforeMessageId, int limit) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Phòng chat không tồn tại"));

        Pageable pageable = PageRequest.of(0, limit);
        Slice<Message> messageSlice;

        if (beforeMessageId == null) {
            // Không có mốc: Lấy tin mới nhất
            messageSlice = messageRepository.findMessagesByConversation(conversationId, pageable);
        } else {
            // Có mốc: Lấy tin cũ hơn mốc đó
            Message beforeMsg = messageRepository.findById(beforeMessageId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy tin nhắn mốc"));

            messageSlice = messageRepository.findMessagesBeforeTime(conversationId, beforeMsg.getCreatedAt(), pageable);
        }

        return messageSlice.map(this::mapToMessageResponse);
    }

    //4 - gui tin nhan
    @Transactional
    public MessageResponse sendMessage(UUID conversationId, UUID senderId, String content, MultipartFile image) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Phòng chat không tồn tại"));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy người gửi"));

        // Xác minh người gửi có nằm trong phòng chat này không
        if (!conversation.getUserOne().getId().equals(senderId) && !conversation.getUserTwo().getId().equals(senderId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Bạn không phải là thành viên của phòng chat này");
        }

        //rang buoc ko duoc de trong khi gui tin nhan
        if ((content == null || content.trim().isEmpty()) && (image == null || image.isEmpty())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST.value(), "Tin nhắn không được để trống");
        }

        String imageUrl = null;
        // 3. Xử lý Upload Ảnh lên Oracle Bucket
        if (image != null && !image.isEmpty()) {
            try {
                // Gọi hàm up ảnh từ OracleStorageService của bạn (trả về 1 chuỗi URL)
                imageUrl = privateChatStorageService.uploadPrivateImage(image);
            } catch (Exception e) {
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Lỗi khi upload ảnh lên hệ thống!");
            }
        }

        // 4. Tạo tin nhắn mới
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content);
        message.setUrlImg(imageUrl);

        Message savedMessage = messageRepository.save(message);

        // 5. Cập nhật lại thông tin Phòng Chat (Đẩy lên top, set last message)
        String lastMsgText = (content != null && !content.trim().isEmpty()) ? content : "Đã gửi 1 ảnh";
        conversation.setLastMessage(lastMsgText);

        // Xử lý thông minh logic đếm tin chưa đọc
        if (conversation.getSeenBy() != null && conversation.getSeenBy().getId().equals(sender.getId())) {
            // Nếu mình tiếp tục nhắn (mình là người seen cuối), thì cộng dồn số tin chưa đọc cho đối phương
            conversation.setUnreadCount(conversation.getUnreadCount() + 1);
        } else {
            // Nếu đối phương vừa nhắn trước đó, giờ mình trả lời lại, thì reset số tin chưa đọc của đối phương về 1
            conversation.setUnreadCount(1);
        }

        conversation.setSeenBy(sender);
        conversationRepository.save(conversation);

        // BẮT ĐẦU: LOGIC BẮN WEBSOCKET REAL-TIME
        // 1. Chuyển đổi Entity thành DTO để gửi đi
        MessageResponse responseDto = mapToMessageResponse(savedMessage);

        // 2. Xác định người nhận là ai (Nếu mình là User 1 thì người nhận là User 2)
        UUID receiverId = conversation.getUserOne().getId().equals(senderId)
                ? conversation.getUserTwo().getId()
                : conversation.getUserOne().getId();

        // 3. Bắn tin nhắn thẳng vào kênh của người nhận
        // Cú pháp: Gửi tới kênh /user/{receiverId}/queue/messages
        messagingTemplate.convertAndSend("/topic/messages/" + receiverId, responseDto);
        // KẾT THÚC: LOGIC BẮN WEBSOCKET

        // 6. Trả về DTO
        return mapToMessageResponse(savedMessage);
    }

    //5 - danh dau da xem
    @Transactional
    public void markAsSeen(UUID conversationId, UUID currentUserId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Phòng chat không tồn tại"));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy User"));

        // BỨC TƯỜNG LỬA: Xác minh người dùng có nằm trong phòng chat này không
        if (!conversation.getUserOne().getId().equals(currentUserId) && !conversation.getUserTwo().getId().equals(currentUserId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thao tác trên phòng chat này");
        }

        // Chỉ cập nhật nếu user hiện tại KHÔNG PHẢI là người đã seen cuối cùng
        // (Để tránh việc người gửi tự seen tin nhắn của chính mình rồi reset unread_count)
        if (conversation.getSeenBy() == null || !conversation.getSeenBy().getId().equals(currentUserId)) {
            conversation.setSeenBy(currentUser);
            conversation.setUnreadCount(0);
            conversationRepository.save(conversation);
        }
    }

    // ==========================================
    // CÁC HÀM HELPER
    // ==========================================

    private User getPartner(Conversation conversation, UUID currentUserId) {
        if (conversation.getUserOne().getId().equals(currentUserId)) {
            return conversation.getUserTwo();
        }
        return conversation.getUserOne();
    }

    private ConversationResponse mapToConversationResponse(Conversation conv, UUID currentUserId) {
        User partner = getPartner(conv, currentUserId);

        ChatUserDto partnerDto = ChatUserDto.builder()
                .userId(partner.getId())
                .name(partner.getName())
                .urlAvt(partner.getAvatar())
                .isOnline(false)
                .build();

        // LOGIC THÔNG MINH XỬ LÝ SỐ TIN CHƯA ĐỌC
        // Nếu mình là người seen cuối cùng (hoặc mình là người vừa gửi tin), thì với mình số tin chưa đọc phải là 0
        int displayUnreadCount = 0;
        if (conv.getSeenBy() != null && !conv.getSeenBy().getId().equals(currentUserId)) {
            displayUnreadCount = conv.getUnreadCount();
        }

        return ConversationResponse.builder()
                .conversationId(conv.getConversationId())
                .partner(partnerDto)
                .lastMessage(conv.getLastMessage())
                .seenBy(conv.getSeenBy() != null ? conv.getSeenBy().getId() : null)
                .unreadCount(displayUnreadCount) // Bỏ cái thông minh này vào đĩa
                .createdAt(conv.getCreatedAt())
                .build();
    }

    private MessageResponse mapToMessageResponse(Message msg) {
        User sender = msg.getSender(); // Đã sửa thành getSender() theo Entity của bạn
        ChatUserDto senderDto = ChatUserDto.builder()
                .userId(sender.getId())
                .name(sender.getName())
                .urlAvt(sender.getAvatar())
                .build();

        return MessageResponse.builder()
                .messageId(msg.getMessageId())
                .content(msg.getContent())
                .urlImg(msg.getUrlImg())
                .createdAt(msg.getCreatedAt())
                .sender(senderDto)
                .build();
    }
}

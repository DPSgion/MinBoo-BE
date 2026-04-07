package com.phobo.chat.controller;

import com.phobo.chat.dto.ConversationRequest;
import com.phobo.chat.dto.ConversationResponse;
import com.phobo.chat.dto.MessageResponse;
import com.phobo.chat.service.ChatService;
import com.phobo.common.exception.BusinessException;
import com.phobo.user.entity.User;
import com.phobo.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/conversations")
public class ChatController {
    @Autowired
    private ChatService chatService;
    @Autowired
    private UserRepository userRepository;

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED.value(), "Vui lòng đăng nhập để sử dụng tính năng này!");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(org.springframework.http.HttpStatus.NOT_FOUND.value(), "Lỗi xác thực: Không tìm thấy tài khoản trong hệ thống"));

        UUID currentUserId = user.getId();
        return currentUserId;
    }

    @PostMapping
    public ResponseEntity<?> createConversation(@Valid @RequestBody ConversationRequest request) {
        UUID currentUserId = getCurrentUserId();
        ConversationResponse response = chatService.createOrGetConversation(currentUserId, request.getUserId());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getConversations() {
        UUID currentUserId = getCurrentUserId();
        List<ConversationResponse> conversations = chatService.getConversations(currentUserId);

        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<?> getMessages(
            @PathVariable UUID conversationId,
            @RequestParam(required = false) UUID before,
            @RequestParam(defaultValue = "30") int limit) {

        Slice<MessageResponse> messages = chatService.getMessages(conversationId, before, limit);
        return ResponseEntity.ok(messages.getContent()); //getContent() de lay list ben trong Slice
    }

    @PostMapping(value = "/{conversationId}/messages", consumes = {"multipart/form-data"})
    public ResponseEntity<?> sendMessage(
            @PathVariable UUID conversationId,
            @RequestParam(required = false) String content,
            @RequestParam(value = "url_img", required = false) MultipartFile image) {

        UUID currentUserId = getCurrentUserId();
        MessageResponse message = chatService.sendMessage(conversationId, currentUserId, content, image);

        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @PostMapping("/{conversationId}/seen")
    public ResponseEntity<?> markAsSeen(@PathVariable UUID conversationId) {
        UUID currentUserId = getCurrentUserId();
        chatService.markAsSeen(conversationId, currentUserId);

        return ResponseEntity.ok().build();
    }
}
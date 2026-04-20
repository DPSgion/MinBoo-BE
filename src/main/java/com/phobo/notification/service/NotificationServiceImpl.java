package com.phobo.notification.service;

import com.phobo.common.exception.BusinessException;
import com.phobo.notification.dto.NotificationResponse;
import com.phobo.notification.entity.Notification;
import com.phobo.notification.entity.NotificationType;
import com.phobo.notification.repository.NotificationRepository;
import com.phobo.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    @Async
    @Transactional
    public void createNotification(User recipient, User sender, NotificationType type, String content, String targetId, String entityType) {
        // 1. Tránh trường hợp tự gửi thông báo cho chính mình (VD: Tự like bài của mình)
        if (recipient.getId().equals(sender.getId())) {
            return;
        }

        // 2. Tạo và lưu vào Database
        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(sender)
                .type(type)
                .entityType(entityType) // <--- SỬ DỤNG BIẾN ENTITY TYPE TRUYỀN VÀO (Không dùng type.name() nữa)
                .content(content)
                .targetId(targetId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        // 3. Đóng gói thành DTO
        NotificationResponse responseDto = mapToResponse(savedNotification);

        // 4. Bắn tín hiệu Real-time qua WebSocket
        // Frontend sẽ lắng nghe ở kênh: /topic/notifications/{userId}
        messagingTemplate.convertAndSend("/topic/notifications/" + recipient.getId(), responseDto);
    }

    @Override
    public List<NotificationResponse> getMyNotifications(UUID currentUserId) {
        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(currentUserId);
        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId, UUID currentUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND.value(), "Không tìm thấy thông báo"));

        // Bảo mật: Chỉ chủ nhân của thông báo mới được đánh dấu đã đọc
        if (!notification.getRecipient().getId().equals(currentUserId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN.value(), "Không có quyền thao tác trên thông báo này");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    // --- HÀM HELPER ---
    private NotificationResponse mapToResponse(Notification notification) {
        User sender = notification.getSender();
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .content(notification.getContent())
                .targetId(notification.getTargetId())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .senderId(sender.getId())
                .senderName(sender.getName())
                .senderAvatar(sender.getAvatar())
                .build();
    }
}
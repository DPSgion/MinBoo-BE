package com.phobo.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private UUID id;
    private String type;
    private String content;
    private String targetId;
    private boolean isRead;
    private LocalDateTime createdAt;

    // Thông tin cơ bản của người tạo ra thông báo để UI hiển thị Avatar/Tên
    private UUID senderId;
    private String senderName;
    private String senderAvatar;
}
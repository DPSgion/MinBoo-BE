package com.phobo.notification.service;

import com.phobo.notification.dto.NotificationResponse;
import com.phobo.notification.entity.NotificationType;
import com.phobo.user.entity.User;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    void createNotification(User recipient, User sender, NotificationType type, String content, String targetId, String entityType);

    List<NotificationResponse> getMyNotifications(UUID currentUserId);

    void markAsRead(UUID notificationId, UUID currentUserId);
}

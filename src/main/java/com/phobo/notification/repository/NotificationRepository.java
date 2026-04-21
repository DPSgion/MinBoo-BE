package com.phobo.notification.repository;

import com.phobo.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    // Lấy thông báo của một người dùng cụ thể, ưu tiên cái mới nhất
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);
}

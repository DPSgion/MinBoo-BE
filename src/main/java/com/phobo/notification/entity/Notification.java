package com.phobo.notification.entity;

import com.phobo.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User recipient;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;    // Người gây ra hành động (người gửi lời mời, người nhắn tin...)

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(name = "entity_type")
    private String entityType;

    private String content;   // Nội dung hiển thị (VD: "A đã gửi lời mời kết bạn")
    @Column(name = "entity_id")
    private String targetId;

    private boolean isRead = false;
    private LocalDateTime createdAt = LocalDateTime.now();
}

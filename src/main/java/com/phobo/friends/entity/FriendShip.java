package com.phobo.friends.entity;

import com.phobo.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "friends_request", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"from_id_a", "to_id_b"})
})
public class FriendShip {

    public enum FriendStatus { PENDING, ACCEPTED, REJECTED }

    @Id
    @Column(name = "friend_request_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer friendRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_id_a")
    User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_id_b")
    User receiver;

    @Column(name = "message")
    String message;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    FriendStatus status;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}

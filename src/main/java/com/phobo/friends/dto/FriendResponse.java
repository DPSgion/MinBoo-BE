package com.phobo.friends.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FriendResponse(
        Integer requestId,
        UUID requesterId,
        String requesterName,
        UUID receiverId,
        String receiverName,
        String message,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

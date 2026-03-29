package com.phobo.friends.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FriendResponse(
        Integer requestId,
        UUID requesterId,
        UUID receiverId,
        String message,
        String status,
        LocalDateTime createdAt
) {
}

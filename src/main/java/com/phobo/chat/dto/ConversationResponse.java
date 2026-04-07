package com.phobo.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ConversationResponse {
    @JsonProperty("conversation_id")
    private UUID conversationId;

    private ChatUserDto partner;

    @JsonProperty("last_message")
    private String lastMessage;

    @JsonProperty("seen_by")
    private UUID seenBy;

    @JsonProperty("unread_count")
    private Integer unreadCount;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}

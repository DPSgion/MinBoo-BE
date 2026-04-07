package com.phobo.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Builder
public class MessageResponse {
    @JsonProperty("message_id")
    private UUID messageId;

    private String content;

    @JsonProperty("url_img")
    private String urlImg;

    @JsonProperty("created_at")
    private ZonedDateTime createdAt;

    private ChatUserDto sender;
}

package com.phobo.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ConversationRequest {
    @NotNull(message = "ID người dùng không được để trống")
    @JsonProperty("user_id")
    private UUID userId;
}

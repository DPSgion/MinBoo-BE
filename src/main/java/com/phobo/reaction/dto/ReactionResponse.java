package com.phobo.reaction.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class ReactionResponse {
    private String action; // Trả về hành động: "ADD", "UPDATE", hoặc "REMOVE"
    private String type;
    private UUID postId;
}
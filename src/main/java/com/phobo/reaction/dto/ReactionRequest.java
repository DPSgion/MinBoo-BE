package com.phobo.reaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ReactionRequest {
    @NotBlank(message = "Loại cảm xúc không được để trống")
    @Pattern(regexp = "^(LIKE|LOVE|HAHA|SAD|ANGRY)$", message = "Cảm xúc phải là: LIKE, LOVE, HAHA, SAD, ANGRY")
    private String type;
}
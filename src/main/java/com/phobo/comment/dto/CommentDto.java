package com.phobo.comment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CommentDto {
    @JsonProperty("comment_id")
    private Long commentId;

    private String content;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    private AuthorDto author;

    @Data
    public static class AuthorDto {
        @JsonProperty("user_id")
        private UUID userId;

        private String name;

        @JsonProperty("url_avt")
        private String urlAvt;

    }
}

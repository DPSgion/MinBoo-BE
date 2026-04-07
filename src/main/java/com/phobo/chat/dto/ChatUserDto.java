package com.phobo.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ChatUserDto {
    @JsonProperty("user_id")
    private UUID userId;

    private String name;

    @JsonProperty("url_avt")
    private String urlAvt;

    // Thuộc tính này API 9.1 cần, nếu API 9.2 không có thì nó tự ẩn đi (trả về null)
    @JsonProperty("is_online")
    private Boolean isOnline;
}

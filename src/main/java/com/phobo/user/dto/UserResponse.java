package com.phobo.user.dto;

import java.time.LocalDate;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String name,
    String username,
    String email,
    String phone,
    Integer sex,
    String address,
    LocalDate birth,
    String avatar,
    String roleName
) {
}

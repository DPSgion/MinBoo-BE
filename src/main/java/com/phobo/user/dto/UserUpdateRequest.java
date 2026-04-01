package com.phobo.user.dto;

import java.time.LocalDate;

public record UserUpdateRequest(
        String name,
        String phone,
        Integer sex,
        LocalDate birth,
        String address,
        String avatar
) {
}

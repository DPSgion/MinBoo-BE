package com.phobo.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record UserRequest(
        @NotBlank(message = "Name is required and must not be blank")
        String name,

        @Email(message = "Email is required and must not be blank")
        String email,

        @NotBlank(message = "Phone is required and must not be blank")
        String phone,

        @NotBlank(message = "Username is required and must not be blank")
        String username,

        @NotBlank(message = "Password is required and must not be blank")
        String password,

        Integer sex, // Must default = 0 or = 1 in UI
        LocalDate birth,
        String address,
        String avatar
) {
}

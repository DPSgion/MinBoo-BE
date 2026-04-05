package com.phobo.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdatePasswordRequest(
        @NotBlank(message = "Current password is required")
        String oldPassword,

        @NotBlank(message = "New password is required")
        String newPassword,

        @NotBlank(message = "Confirmation password is required")
        String confirmPassword
) {
}

package com.phobo.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(

        @NotBlank(message = "Username can not be null")
        String username,

        @NotBlank(message = "Password can not be null")
        String password

) {
}

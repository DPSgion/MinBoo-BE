package com.phobo.user.service;

import com.phobo.common.response.PageResponse;
import com.phobo.user.dto.UserResponse;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    PageResponse<UserResponse> findAll(String search, int page, int size);

    Optional<UserResponse> findByID(UUID id);
}

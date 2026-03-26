package com.phobo.user.service;

import com.phobo.common.response.PageResponse;
import com.phobo.user.dto.UserRequest;
import com.phobo.user.dto.UserResponse;
import com.phobo.user.dto.UserUpdatePasswordRequest;
import com.phobo.user.dto.UserUpdateRequest;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    PageResponse<UserResponse> findAll(String search, int page, int size);

    UserResponse findByID(UUID id);

    UserResponse create(UserRequest createRequest);

    UserResponse update(UserUpdateRequest updateRequest, UUID id);

    void updatePassword(UserUpdatePasswordRequest updatePasswordRequest, UUID id);

    void deleteUser(UUID id);
}

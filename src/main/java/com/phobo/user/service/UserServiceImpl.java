package com.phobo.user.service;

import com.phobo.common.response.PageResponse;
import com.phobo.user.dto.UserResponse;
import com.phobo.user.mapper.UserMapper;
import com.phobo.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public PageResponse<UserResponse> findAll(String search, int page, int size) {
        String term = (search != null && !search.isBlank()) ? search.strip() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        org.springframework.data.domain.Page<com.phobo.user.entity.User> userPage;

        if (term != null) {
            userPage = userRepository.searchUsers(term, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        return PageResponse.from(userPage.map(userMapper::toResponse));
    }

    @Override
    public Optional<UserResponse> findByID(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse);
    }

}

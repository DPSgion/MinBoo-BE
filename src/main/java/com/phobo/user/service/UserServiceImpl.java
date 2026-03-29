package com.phobo.user.service;

import com.phobo.common.exception.BusinessException;
import com.phobo.common.exception.ResourceNotFoundException;
import com.phobo.common.response.PageResponse;
import com.phobo.user.dto.UserRequest;
import com.phobo.user.dto.UserResponse;
import com.phobo.user.dto.UserUpdatePasswordRequest;
import com.phobo.user.dto.UserUpdateRequest;
import com.phobo.user.entity.User;
import com.phobo.user.mapper.UserMapper;
import com.phobo.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageResponse<UserResponse> findAll(String search, int page, int size) {
        String term = (search != null && !search.isBlank()) ? search.strip() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        org.springframework.data.domain.Page<User> userPage;

        if (term != null) {
            userPage = userRepository.searchUsers(term, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        return PageResponse.from(userPage.map(userMapper::toResponse));
    }


    @Override
    public UserResponse findByID(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }


    @Override
    @Transactional
    public UserResponse create(UserRequest userRequest) {

        if (userRepository.existsByPhone(userRequest.phone())){
            throw new BusinessException(409, "Phone already exists");
        }
        if (userRepository.existsByEmail(userRequest.email())){
            throw new BusinessException(409, "Email already exist");
        }
        if (userRepository.existsByUsername(userRequest.username())){
            throw new BusinessException(409, "Username already exists");
        }

        User user = userMapper.toEntity(userRequest);

        String hashedPassword = passwordEncoder.encode(userRequest.password());

        user.setPassword(hashedPassword);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse update(UserUpdateRequest userUpdateRequest, UUID id) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));


        if (userUpdateRequest.phone() != null && !userUpdateRequest.phone().isBlank()) {
            if (userRepository.existsByPhoneAndIdNot(userUpdateRequest.phone(), id)){
                throw new BusinessException(409, "Phone already exists");
            }
        }

        userMapper.updateEntity(existingUser, userUpdateRequest);

        return userMapper.toResponse(userRepository.save(existingUser));
    }

    @Override
    @Transactional
    public void updatePassword(UserUpdatePasswordRequest userUpdatePasswordRequest, UUID id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (!userUpdatePasswordRequest.newPassword().equals(userUpdatePasswordRequest.confirmPassword())){
            throw new BusinessException(400, "New password don't match confirm password");
        }

        if (!passwordEncoder.matches(userUpdatePasswordRequest.oldPassword(), existingUser.getPassword())){
            throw new BusinessException(400, "Old Password is Incorrect");
        }

        existingUser.setPassword(passwordEncoder.encode(userUpdatePasswordRequest.newPassword()));
        userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        userRepository.delete(existingUser);
    }


}

package com.phobo.auth.service;

import com.phobo.auth.dto.AuthRequest;
import com.phobo.auth.dto.AuthResponse;
import com.phobo.auth.dto.RegisterRequest;
import com.phobo.auth.dto.RegisterResponse;
import com.phobo.common.exception.BusinessException;
import com.phobo.common.oci.ImageStorageService;
import com.phobo.security.JwtUtil;
import com.phobo.user.entity.User;
import com.phobo.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageStorageService imageStorageService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository, PasswordEncoder passwordEncoder, ImageStorageService imageStorageService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.imageStorageService = imageStorageService;
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        // Ném cho Spring Security tự kiểm tra, sai pass nó tự ném Exception
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateToken(authentication.getName());
        return new AuthResponse(jwt);
    }

    @Transactional
    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByPhone(registerRequest.phone())){
            throw new BusinessException(409, "Phone already exists");
        }
        if (userRepository.existsByEmail(registerRequest.email())){
            throw new BusinessException(409, "Email already exist");
        }
        if (userRepository.existsByUsername(registerRequest.username())){
            throw new BusinessException(409, "Username already exists");
        }

        User user = new User();
        user.setName(registerRequest.name());
        user.setEmail(registerRequest.email());
        user.setPhone(registerRequest.phone());
        user.setUsername(registerRequest.username());

        String hashedPassword = passwordEncoder.encode(registerRequest.password());

        user.setPassword(hashedPassword);

        user.setSex(registerRequest.sex() != null ? registerRequest.sex() : 0);
        user.setBirth(registerRequest.birth());
        user.setAddress(registerRequest.address());

        String avatarInput = "";

        if (registerRequest.avatar() == null || registerRequest.avatar().isEmpty()) {
            avatarInput = "https://api.dicebear.com/8.x/adventurer/svg?seed=" + registerRequest.username();
        } else {
            try {
                avatarInput = imageStorageService.uploadImage(registerRequest.avatar(), "avatars");
            }
            catch (Exception e) {
                throw new BusinessException(500, "Error uploading avatar image to Cloud: " + e.getMessage());
            }
        }
        user.setAvatar(avatarInput);
        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                savedUser.getSex(),
                savedUser.getAddress(),
                savedUser.getBirth(),
                savedUser.getAvatar()
        );
    }
}
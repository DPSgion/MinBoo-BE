package com.phobo.auth.service;

import com.phobo.auth.dto.AuthRequest;
import com.phobo.auth.dto.AuthResponse;
import com.phobo.auth.dto.RegisterRequest;
import com.phobo.auth.dto.RegisterResponse;

public interface AuthService {

    AuthResponse login(AuthRequest request);

    RegisterResponse register(RegisterRequest registerRequest);

}

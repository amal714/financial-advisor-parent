package com.financial.advisor.user.service;

import com.financial.advisor.user.dto.AuthRequest;
import com.financial.advisor.user.dto.AuthResponse;

public interface AuthService {
    AuthResponse register(AuthRequest request);
    AuthResponse login(AuthRequest request);
}
package com.financial.advisor.user.service;

import com.financial.advisor.common.exception.BusinessException;
import com.financial.advisor.common.security.JwtUtils;
import com.financial.advisor.user.domain.User;
import com.financial.advisor.user.dto.AuthRequest;
import com.financial.advisor.user.dto.AuthResponse;
import com.financial.advisor.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public AuthResponse register(AuthRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessException("Email is already registered", 409);
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        User newUser = new User(request.email(), hashedPassword, "ROLE_USER");
        User savedUser = userRepository.save(newUser);

        String token = jwtUtils.generateToken(savedUser.getId(), savedUser.getRole());
        return new AuthResponse(token, "Bearer", savedUser.getId(), savedUser.getRole());
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Invalid email or password", 401));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("Invalid email or password", 401);
        }

        String token = jwtUtils.generateToken(user.getId(), user.getRole());
        return new AuthResponse(token, "Bearer", user.getId(), user.getRole());
    }
}
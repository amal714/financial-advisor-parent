package com.financial.advisor.user.service;

import com.financial.advisor.common.exception.BusinessException;
import com.financial.advisor.common.security.JwtUtils;
import com.financial.advisor.user.domain.User;
import com.financial.advisor.user.dto.AuthRequest;
import com.financial.advisor.user.dto.AuthResponse;
import com.financial.advisor.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthServiceImpl authService;

    private AuthRequest request;
    private User mockUser;

    @BeforeEach
    void setUp() {
        request = new AuthRequest("test@test.com", "password123");
        mockUser = new User("test@test.com", "hashedPassword", "ROLE_USER");
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtUtils.generateToken(any(), anyString())).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.token());
        assertEquals("Bearer", response.type());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenRegisteringExistingEmail() {
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(mockUser));

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.register(request));
        assertEquals(409, exception.getStatus());
        assertEquals("Email is already registered", exception.getMessage());
    }

    @Test
    void shouldLoginSuccessfully() {
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(request.password(), mockUser.getPasswordHash())).thenReturn(true);
        when(jwtUtils.generateToken(any(), anyString())).thenReturn("mock-jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.token());
    }

    @Test
    void shouldThrowExceptionOnLoginWithInvalidPassword() {
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(request.password(), mockUser.getPasswordHash())).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(request));
        assertEquals(401, exception.getStatus());
    }


}
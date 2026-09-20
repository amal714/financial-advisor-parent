package com.financial.advisor.user.dto;

public record AuthResponse(String token, String type, String userId, String role) {}
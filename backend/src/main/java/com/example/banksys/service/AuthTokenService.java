package com.example.banksys.service;

import org.springframework.stereotype.Service;

@Service
public class AuthTokenService {

    private static final String PREFIX = "mock-";

    public String generateToken(Long employeeId) {
        return PREFIX + employeeId;
    }

    public Long parseEmployeeId(String token) {
        if (token == null || !token.startsWith(PREFIX)) {
            return null;
        }
        try {
            String idPart = token.substring(PREFIX.length());
            return Long.parseLong(idPart);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
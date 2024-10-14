package com.example.demo.service;

import com.example.demo.entity.LoginAttempt;
import com.example.demo.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginAttemptServiceUsingIpAddress {
    private final LoginAttemptRepository loginAttemptRepository;

    private static final int MAX_ATTEMPT = 10;
    private static final long BLOCK_DURATION_MINUTES = 24*60;

    public void loginFailed(String ipAddress) {
        LoginAttempt loginAttempt = loginAttemptRepository.findByIpAddress(ipAddress)
                .orElse(new LoginAttempt(ipAddress));

        loginAttempt.setAttemptCount(loginAttempt.getAttemptCount() + 1);
        loginAttempt.setLastAttempt(LocalDateTime.now());

        if (loginAttempt.getAttemptCount() >= MAX_ATTEMPT) {
            loginAttempt.setBlockedUntil(LocalDateTime.now().plusMinutes(BLOCK_DURATION_MINUTES));
        }

        loginAttemptRepository.save(loginAttempt);
    }

    public boolean isBlocked(String ipAddress) {
        Optional<LoginAttempt> loginAttempt = loginAttemptRepository.findByIpAddress(ipAddress);
        return loginAttempt.isPresent() && loginAttempt.get().getBlockedUntil().isAfter(LocalDateTime.now());
    }

    public void loginSucceeded(String ipAddress) {
        loginAttemptRepository.deleteByIpAddress(ipAddress);
    }
}

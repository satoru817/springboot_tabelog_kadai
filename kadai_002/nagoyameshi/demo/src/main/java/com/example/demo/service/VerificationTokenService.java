package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.entity.VerificationToken;
import com.example.demo.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {
    private final VerificationTokenRepository verificationTokenRepository;

    @Transactional
    public void create(User user, String token){
        VerificationToken verificationToken = new VerificationToken();

        verificationToken.setUser(user);
        verificationToken.setToken(token);

        verificationTokenRepository.save(verificationToken);
    }

    public Optional<VerificationToken> getVerificationTokenByToken(String token){
        return verificationTokenRepository.findByToken(token);
    }

    public Optional<VerificationToken> getVerificationTokenByUser(User user){
        return verificationTokenRepository.findByUser(user);
    }

    public Optional<VerificationToken> findByUser(User user) {
        return verificationTokenRepository.findByUser(user);
    }

    public void save(VerificationToken verificationToken){
        verificationTokenRepository.save(verificationToken);
    }
}

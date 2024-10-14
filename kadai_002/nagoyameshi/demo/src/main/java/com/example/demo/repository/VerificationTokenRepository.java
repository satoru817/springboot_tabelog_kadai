package com.example.demo.repository;

import com.example.demo.entity.User;
import com.example.demo.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken,Integer> {
    public Optional<VerificationToken> findByToken(String token);

    public Optional<VerificationToken> findByUser(User user);
}

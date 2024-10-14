package com.example.demo.repository;

import com.example.demo.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Integer> {
    Optional<LoginAttempt> findByIpAddress(String ipAddress);

    @Modifying
    @Transactional
    void deleteByIpAddress(String ipAddress);

    @Modifying
    @Transactional
    @Query("DELETE FROM LoginAttempt la WHERE la.lastAttempt < :localDateTime")
    void deleteAttemptsOlderThan(LocalDateTime localDateTime);
}

package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "login_attempts")
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0; // デフォルト値を設定

    @Column(name = "last_attempt")
    private LocalDateTime lastAttempt; // LocalDateTimeに変更

    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil; // LocalDateTimeに変更

    public LoginAttempt() {
        // 引数付きコンストラクタを書いてしまったら、
        // Lombokはデフォルトコンストラクタを自動生成してくれないから明示的に書いている
    }
    // コンストラクタ
    public LoginAttempt(String ipAddress) {
        this.ipAddress = ipAddress;
        this.attemptCount = 1; // 初回ログイン試行のため、attemptCountを1に設定
        this.lastAttempt = LocalDateTime.now(); // 現在の日時を設定
    }
}

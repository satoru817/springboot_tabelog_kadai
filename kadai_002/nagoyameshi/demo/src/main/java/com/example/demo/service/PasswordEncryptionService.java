package com.example.demo.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordEncryptionService {

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // パスワードをBCryptで暗号化するメソッド
    public String encryptPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

}

package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final ImageService imageService;

    public void replaceField(User exUser, User newUser) throws IOException {
        // 基本情報の更新
        exUser.setName(newUser.getName());
        exUser.setNameForReservation(newUser.getNameForReservation());
        exUser.setEmail(newUser.getEmail());
        exUser.setPostalCode(newUser.getPostalCode());
        exUser.setAddress(newUser.getAddress());
        exUser.setPhoneNumber(newUser.getPhoneNumber());

        // アイコン画像が新しく設定されている場合のみ更新
        if (newUser.getIcon() != null && !newUser.getIcon().isEmpty()) {
            String fileName = imageService.saveImage(newUser.getIcon(), newUser.getName());
            exUser.setProfileImage(fileName);
        }


    }
}
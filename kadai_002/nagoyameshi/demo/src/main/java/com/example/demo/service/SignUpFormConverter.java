package com.example.demo.service;

import com.example.demo.dto.SignUpForm;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class SignUpFormConverter {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncryptionService passwordEncryptionService;
    private final ImageService imageService;
    //同じメールアドレス、同じ名前のユーザーがすでにいる場合はnullを返す
    // そうでなければUserオブジェクトを返す。
    public Optional<User> singUpFormToUnpaidUser(SignUpForm form) throws IOException {
        if(userRepository.findByNameOrEmail(form.getName(), form.getEmail()).isPresent()){
            return Optional.empty();
        }else{
            User user = new User();
            Optional<Role> optionalUnpaid = roleRepository.findRoleByName("ROLE_UNPAID_USER");
            optionalUnpaid.ifPresent(user::setRole);
            user.setName(form.getName());
            user.setNameForReservation(form.getNameForReservation());
            user.setPassword(passwordEncryptionService.encryptPassword(form.getPassword()));
            user.setEmail(form.getEmail());
            user.setPostalCode(form.getPostalCode());
            user.setAddress(form.getAddress());
            user.setPhoneNumber(form.getPhoneNumber());
            //Base64の画像を保存してファイル名を得ている。
            String fileName = imageService.saveImage(form.getIcon(),form.getName());
            user.setProfileImage(fileName);
            return Optional.of(user);
        }
    }
}

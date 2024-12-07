package com.example.demo.service;

import com.example.demo.constants.RoleName;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.error.UserNotFoundException;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final ImageService imageService;
    private final RoleService roleService;

    @Transactional(readOnly = true)
    public User findById(Integer userId){
        return userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException("User not found with id:"+userId));
    }


    //画像データの削除を追加する必要がある。
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
            imageService.deleteImage(exUser.getProfileImage());
            String fileName = imageService.saveImage(newUser.getIcon(), newUser.getName());
            exUser.setProfileImage(fileName);
        }


    }

    public void togglePaymentStatus(User user){
        if(user.getRole().getName().equals(RoleName.ADMIN.getRoleName())){
            return;
        }

        if(user.getRole().getName().equals(RoleName.PAID.getRoleName())){
            roleService.setUnpaid(user);
            userRepository.save(user);
            return;
        }

        if(user.getRole().getName().equals(RoleName.UNPAID.getRoleName())){
            roleService.setPaid(user);
            userRepository.save(user);

        }
    }
}

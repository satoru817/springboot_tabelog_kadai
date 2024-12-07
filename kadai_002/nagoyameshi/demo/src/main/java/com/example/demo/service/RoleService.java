package com.example.demo.service;

import com.example.demo.constants.RoleName;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;


    public void setUnpaid(User user) {
        Role unpaid = roleRepository.findRoleByName(RoleName.UNPAID.getRoleName())
                .orElseThrow(()->new RuntimeException("指定されたroleが見つかりませんでした。"));

        user.setRole(unpaid);
    }

    public void setPaid(User user) {
        Role paid = roleRepository.findRoleByName(RoleName.PAID.getRoleName())
                .orElseThrow(()->new RuntimeException("指定されたroleが見つかりませんでした。"));

        user.setRole(paid);
    }
}

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

    private static final String ROLE_UNPAID_USER = "ROLE_UNPAID_USER";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    public Optional<User> singUpFormToUnpaidUser(SignUpForm form) throws IOException {
        return createUser(form, ROLE_UNPAID_USER);
    }

    public Optional<User> singUpFormToAdmin(SignUpForm form) throws IOException {
        return createUser(form, ROLE_ADMIN);
    }

    private Optional<User> createUser(SignUpForm form, String roleName) throws IOException {
        if (isUserExists(form)) {
            return Optional.empty();
        }

        User user = new User();
        setFields(form, user, roleName);
        return Optional.of(user);
    }

    private boolean isUserExists(SignUpForm form) {
        return userRepository.findByNameOrEmail(form.getName(), form.getEmail()).isPresent();
    }

    private void setFields(SignUpForm form, User user, String roleName) throws IOException {
        setRole(user, roleName);
        setUserDetails(form, user);
        setUserImage(form, user);
    }

    private void setRole(User user, String roleName) {
        Optional<Role> role = roleRepository.findRoleByName(roleName);
        role.ifPresent(user::setRole);
    }

    private void setUserDetails(SignUpForm form, User user) {
        user.setName(form.getName());
        user.setNameForReservation(form.getNameForReservation());
        user.setPassword(passwordEncryptionService.encryptPassword(form.getPassword()));
        user.setEmail(form.getEmail());
        user.setPostalCode(form.getPostalCode());
        user.setAddress(form.getAddress());
        user.setPhoneNumber(form.getPhoneNumber());
    }

    private void setUserImage(SignUpForm form, User user) throws IOException {
        String fileName = imageService.saveImage(form.getIcon(), form.getName());
        user.setProfileImage(fileName);
    }
}

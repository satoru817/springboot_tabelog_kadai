package com.example.demo.repository;

import com.example.demo.constants.RoleName;
import com.example.demo.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Integer> {
    Optional<Role> findRoleByName(String roleName);


}

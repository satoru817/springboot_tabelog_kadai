package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findByName(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByNameOrEmail(String name,String email);

    Optional<User> findByStripeCustomerId(String stripeCustomerId);

    boolean existsByName(String name);
}

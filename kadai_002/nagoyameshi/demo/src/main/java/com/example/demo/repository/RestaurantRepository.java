package com.example.demo.repository;

import com.example.demo.entity.Restaurant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant,Integer> {
    Optional<Restaurant> findByEmailOrPhoneNumber( String email, String phoneNumber);

    Page<Restaurant> findByNameLike(String s, Pageable pageable);
}

package com.example.demo.repository;

import com.example.demo.entity.RestaurantImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantImageRepository extends JpaRepository<RestaurantImage,Integer> {
}

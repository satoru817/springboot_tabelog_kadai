package com.example.demo.repository;

import com.example.demo.entity.Restaurant;
import com.example.demo.entity.RestaurantImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantImageRepository extends JpaRepository<RestaurantImage,Integer> {
    List<RestaurantImage> findAllByRestaurant(Restaurant restaurant);
}

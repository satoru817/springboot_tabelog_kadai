package com.example.demo.repository;

import com.example.demo.entity.CategoryRestaurant;
import com.example.demo.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRestaurantRepository extends JpaRepository<CategoryRestaurant, Integer> {

    List<CategoryRestaurant> findAllByRestaurant(Restaurant restaurant);
}


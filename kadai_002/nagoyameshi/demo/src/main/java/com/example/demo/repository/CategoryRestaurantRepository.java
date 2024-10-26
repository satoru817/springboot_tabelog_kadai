package com.example.demo.repository;

import com.example.demo.entity.CategoryRestaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRestaurantRepository extends JpaRepository<CategoryRestaurant, Integer> {

}


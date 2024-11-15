package com.example.demo.repository;

import com.example.demo.entity.Favorite;
import com.example.demo.entity.Restaurant;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite,Integer> {


    Boolean existsByUserAndRestaurant(User user, Restaurant restaurant);

    void deleteByUserAndRestaurant(User user, Restaurant restaurant);
}

package com.example.demo.repository;

import com.example.demo.entity.Restaurant;
import com.example.demo.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant,Integer> , JpaSpecificationExecutor<Restaurant> {
    Optional<Restaurant> findByEmailOrPhoneNumber( String email, String phoneNumber);

    Page<Restaurant> findByNameLike(String s, Pageable pageable);

    @Query("""
            SELECT ret FROM Favorite f
            JOIN f.restaurant ret 
            JOIN ret.categoryRestaurants cr 
            JOIN cr.category c
            WHERE f.user = :user 
            AND (ret.name LIKE %:searchQuery% 
            OR ret.description LIKE %:searchQuery%
            OR ret.address LIKE %:searchQuery%
            OR c.categoryName LIKE %:searchQuery%)
            """)
    Page<Restaurant> findAllFavoriteBySearchQueryAndUser(@Param("searchQuery")String searchQuery,
                                                         @Param("user")User user,
                                                         Pageable pageable);

    @Query("""
            SELECT ret FROM Favorite f 
            JOIN f.restaurant ret
            WHERE f.user = :user
            """)
    Page<Restaurant> findAllFavoriteByUser(@Param("user")User user, Pageable pageable);
}

package com.example.demo.repository;

import com.example.demo.entity.Reservation;
import com.example.demo.entity.Restaurant;
import com.example.demo.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review,Integer> {
    Optional<Review> findByReservation(Reservation reservation);

    @Query("SELECT AVG(rev.starCount) FROM Review rev " +
            "JOIN rev.reservation res " +
            "WHERE res.restaurant = :restaurant")
    Optional<Float> getAverageStarForRestaurant(Restaurant restaurant);

    @Query("SELECT rev FROM Review rev " +
            "WHERE rev.reservation.user.userId = :userId")
    Page<Review> findAllByUserId(@Param("userId")Integer userId, Pageable pageable);
}

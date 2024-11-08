package com.example.demo.repository;

import com.example.demo.entity.Reservation;
import com.example.demo.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review,Integer> {
    Optional<Review> findByReservation(Reservation reservation);
}

package com.example.demo.repository;

import com.example.demo.entity.Reservation;
import com.example.demo.entity.Restaurant;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReservationRepository extends JpaRepository<Reservation,Integer> {

    @Query("SELECT COUNT(re) FROM Reservation re WHERE " +
            "re.restaurant = :restaurant " +
            "AND re.date BETWEEN :intervalBefore AND :reservationDateTime")
    Integer countReservation(@Param("restaurant")Restaurant restaurant,
                             @Param("reservationDateTime") LocalDateTime reservationDateTime,
                             @Param("intervalBefore")LocalDateTime intervalBefore);

    //一覧画面(reservation/show)で使うメソッド
    Page<Reservation> findAllByUser(User user, Pageable pageable);
}

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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Integer>, JpaSpecificationExecutor<Restaurant> {
    Optional<Restaurant> findByEmailOrPhoneNumber(String email, String phoneNumber);

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
    Page<Restaurant> findAllFavoriteBySearchQueryAndUser(@Param("searchQuery") String searchQuery,
                                                         @Param("user") User user,
                                                         Pageable pageable);

    @Query("""
            SELECT ret FROM Favorite f 
            JOIN f.restaurant ret
            WHERE f.user = :user
            """)
    Page<Restaurant> findAllFavoriteByUser(@Param("user") User user, Pageable pageable);

    @Query("""
            SELECT ret 
            FROM Restaurant ret
            LEFT JOIN Reservation res ON res.restaurant = ret
            LEFT JOIN Review rev ON rev.reservation = res
            WHERE res.date > :oneMonthAgo
            GROUP BY ret
            ORDER BY COALESCE(AVG(rev.starCount), 0) DESC
            LIMIT :i
            """)
    List<Restaurant> findTopRated(@Param("i") Integer i, @Param("oneMonthAgo") LocalDateTime oneMonthAgo);
    //直近30日のデータのみ利用する。レビューの星評価の平均が高い順で並べる。

    @Query("""
            SELECT ret
            FROM Restaurant ret
            LEFT JOIN Favorite f ON f.restaurant = ret
            GROUP BY ret
            ORDER BY COALESCE(COUNT(DISTINCT f.favoriteId),0) DESC
            LIMIT :i
            """)
    List<Restaurant> findTopFavorited(@Param("i") Integer i);

    @Query("""
            SELECT ret
            FROM Restaurant ret
            LEFT JOIN Reservation res ON res.restaurant = ret
            LEFT JOIN Review rev ON rev.reservation = res
            WHERE res.date > :oneMonthAgo
            GROUP BY ret
            ORDER BY COALESCE(COUNT(DISTINCT rev.reviewId),0) DESC
            LIMIT :i
            """)
    List<Restaurant> findTopReviewed(@Param("i") Integer i, @Param("oneMonthAgo") LocalDateTime oneMonthAgo);
}

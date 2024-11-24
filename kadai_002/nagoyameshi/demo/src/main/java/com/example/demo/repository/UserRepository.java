package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findByName(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByNameOrEmail(String name,String email);

    Optional<User> findByStripeCustomerId(String stripeCustomerId);

    boolean existsByName(String name);

    boolean existsByEmail(String email);

    @Query("""
            SELECT DISTINCT u FROM User u
            INNER JOIN u.role r
            WHERE LOWER(u.name) LIKE LOWER(CONCAT('%',:keyword,'%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%',:keyword,'%'))
            OR u.postalCode LIKE %:keyword%
            OR LOWER(u.address) LIKE LOWER (CONCAT('%',:keyword,'%'))
            OR u.phoneNumber LIKE %:keyword%
            OR LOWER(r.name) LIKE LOWER (CONCAT('%',:keyword,'%'))
            """)
    Page<User> findAllByKeyWord(@Param("keyword")String keyword,
                                Pageable pageable);
}

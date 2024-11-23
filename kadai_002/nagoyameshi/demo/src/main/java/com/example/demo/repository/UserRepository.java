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
            SELECT u FROM User u WHERE
            u.name LIKE %:keyword%
            OR u.email LIKE %:keyword%
            OR u.postalCode LIKE %:keyword%
            OR u.address LIKE %:keyword%
            OR u.phoneNumber LIKE %:keyword%
            OR u.role.name LIKE %:keyword%
            """)
    Page<User> findAllByKeyWord(@Param("keyword")String keyword,
                                Pageable pageable);
}

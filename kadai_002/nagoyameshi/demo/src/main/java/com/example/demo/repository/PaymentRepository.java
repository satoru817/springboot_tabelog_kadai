package com.example.demo.repository;

import com.example.demo.entity.Payment;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Long> {
    boolean existsByStripePaymentIntentId(String stripePaymentIntentId);

    @Query("""
            SELECT p FROM Payment p 
            WHERE p.user.name LIKE %:searchQuery%
            OR p.user.email LIKE %:searchQuery%
            """)
    Page<Payment> findAllBySearchQuery(String searchQuery, Pageable pageable);
}

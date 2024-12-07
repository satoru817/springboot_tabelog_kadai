package com.example.demo.repository;

import com.example.demo.entity.Payment;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Long> {
    boolean existsByStripePaymentIntentId(String stripePaymentIntentId);


    @Query("""
            SELECT p FROM Payment p
            JOIN p.user user
            WHERE user.name LIKE %:userName%
            AND user.email LIKE %:email%
            AND DATE(p.createdAt) = :date
            """)
    Page<Payment> findAllByNameAndEmailAndDate(
            @Param("userName") String userName,
            @Param("email") String email,
            @Param("date") LocalDate date,
            Pageable pageable);

    @Query("""
            SELECT p FROM Payment p
            JOIN p.user user
            WHERE user.name LIKE %:userName%
            AND user.email LIKE %:email%
            """)
    Page<Payment> findAllByNameAndEmail(
            @Param("userName") String userName,
            @Param("email") String email,
            Pageable pageable);

    @Query("""
            SELECT p FROM Payment p
            JOIN p.user user
            WHERE user.name LIKE %:userName%
            AND DATE(p.createdAt) = :date
            """)
    Page<Payment> findAllByNameAndDate(
            @Param("userName") String userName,
            @Param("date") LocalDate date,
            Pageable pageable);

    @Query("""
            SELECT p FROM Payment p
            JOIN p.user user
            WHERE user.email LIKE %:email%
            AND DATE(p.createdAt) = :date
            """)
    Page<Payment> findAllByEmailAndDate(
            @Param("email") String email,
            @Param("date") LocalDate date,
            Pageable pageable);

    @Query("""
            SELECT p FROM Payment p
            JOIN p.user user
            WHERE user.name LIKE %:userName%
            """)
    Page<Payment> findAllByName(
            @Param("userName") String userName,
            Pageable pageable);

    @Query("""
            SELECT p FROM Payment p
            JOIN p.user user
            WHERE user.email LIKE %:email%
            """)
    Page<Payment> findAllByEmail(
            @Param("email") String email,
            Pageable pageable);

    @Query("""
            SELECT p FROM Payment p
            WHERE DATE(p.createdAt) = :date
            """)
    Page<Payment> findAllByDate(
            @Param("date") LocalDate date,
            Pageable pageable);
}

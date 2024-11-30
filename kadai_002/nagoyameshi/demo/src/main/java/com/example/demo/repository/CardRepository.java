package com.example.demo.repository;

import com.example.demo.entity.Card;
import com.example.demo.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card,Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Card> findByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Card> getByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Card> findByUserAndIsDefault(User user,Boolean isDefault);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Card> findByStripeCardId(String paymentMethodId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Card> findByUserAndIsDefaultTrue(User user);
}

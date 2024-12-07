package com.example.demo.repository;

import com.example.demo.entity.Subscription;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription,Integer> {
    Optional<Subscription> findByUser(User user);

    Optional<Subscription> findByStripeSubscriptionId(String subscriptionId);
}

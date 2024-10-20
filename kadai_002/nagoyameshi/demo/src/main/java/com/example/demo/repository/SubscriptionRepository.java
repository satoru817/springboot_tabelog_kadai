package com.example.demo.repository;

import com.example.demo.entity.Subscription;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription,Integer> {
    Subscription findByUser(User user);

    Subscription findByStripeSubscriptionId(String subscriptionId);
}

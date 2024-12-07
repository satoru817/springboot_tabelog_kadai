package com.example.demo.service;

import com.example.demo.entity.Subscription;
import com.example.demo.entity.User;
import com.example.demo.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    public Subscription findByUser(User user){
        return subscriptionRepository.findByUser(user)
                .orElseThrow(()-> new RuntimeException("該当のsubscriptionが見つかりませんでした"));
    }

    public Subscription findByStripeSubscriptionId(String subscriptionId){
        return subscriptionRepository.findByStripeSubscriptionId(subscriptionId)
                .orElseThrow(()-> new RuntimeException("該当のさぶすくりぷしょんが 見つかりませんでした。"));
    }
}

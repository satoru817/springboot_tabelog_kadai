package com.example.demo.repository;

import com.example.demo.entity.Card;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card,Integer> {
    List<Card> findByUser(User user);
}

package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cards", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "stripeCardId"})
})
@Data
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="card_id")
    private Long cardId;

    @ManyToOne
    @JoinColumn(name="user_id",nullable = false)
    private User user;

    @Column(name="stripe_card_id",nullable = false, length = 255)
    private String stripeCardId;

    @Column(name="brand",length = 50)
    private String brand;

    @Column(name="last4",length = 4)
    private String last4;

    @Column(name="exp_month")
    private byte expMonth;

    @Column(name="exp_year")
    private short expYear;

    @Column(name="is_default",nullable = false)
    private Boolean isDefault = false;

    @Column(name="created_at",nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name="updated_at",nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

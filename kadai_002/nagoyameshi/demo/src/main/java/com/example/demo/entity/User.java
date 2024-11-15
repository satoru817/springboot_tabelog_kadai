package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Integer userId;

    @ManyToOne
    @JoinColumn(name="role_id")
    private Role role;

    @Column(name="name", unique = true, nullable = false)
    private String name;

    @Column(name="stripe_customer_id",nullable = true)
    private String stripeCustomerId;

    @Column(name="name_for_reservation")
    private String nameForReservation;

    @Column(name="password", nullable = false)
    private String password;

    @Column(name="email", unique = true, nullable = false)
    private String email;

    @Column(name="postal_code", length = 10)
    private String postalCode;

    @Column(name="address", length = 255)
    private String address;

    @Column(name="phone_number", length = 20)
    private String phoneNumber;

    @Column(name="profile_image" , length=255)
    private String profileImage;

    @Column(name="enabled",nullable = false)
    private Boolean enabled;

    @CreationTimestamp
    @Column(name="created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at")
    private LocalDateTime updatedAt;

    @Transient
    private String icon;//base64のimageStringを送るためのフィールド
}

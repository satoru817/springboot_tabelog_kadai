package com.example.demo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer restaurantId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "image_name")
    private String imageName;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;
}

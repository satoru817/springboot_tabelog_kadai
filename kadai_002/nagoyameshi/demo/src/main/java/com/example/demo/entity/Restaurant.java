package com.example.demo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalTime;

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

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    // 曜日ごとの開店時間
    @Column(name = "monday_opening_time")
    private LocalTime mondayOpeningTime;

    @Column(name = "monday_closing_time")
    private LocalTime mondayClosingTime;

    @Column(name = "tuesday_opening_time")
    private LocalTime tuesdayOpeningTime;

    @Column(name = "tuesday_closing_time")
    private LocalTime tuesdayClosingTime;

    @Column(name = "wednesday_opening_time")
    private LocalTime wednesdayOpeningTime;

    @Column(name = "wednesday_closing_time")
    private LocalTime wednesdayClosingTime;

    @Column(name = "thursday_opening_time")
    private LocalTime thursdayOpeningTime;

    @Column(name = "thursday_closing_time")
    private LocalTime thursdayClosingTime;

    @Column(name = "friday_opening_time")
    private LocalTime fridayOpeningTime;

    @Column(name = "friday_closing_time")
    private LocalTime fridayClosingTime;

    @Column(name = "saturday_opening_time")
    private LocalTime saturdayOpeningTime;

    @Column(name = "saturday_closing_time")
    private LocalTime saturdayClosingTime;

    @Column(name = "sunday_opening_time")
    private LocalTime sundayOpeningTime;

    @Column(name = "sunday_closing_time")
    private LocalTime sundayClosingTime;
}

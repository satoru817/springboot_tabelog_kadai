package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static jakarta.persistence.FetchType.*;

@Data
@Entity
@Table(name="reservations")
public class Reservation {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="reservation_id")
    private Integer reservationId;

    @ManyToOne
    @JoinColumn(name="restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @Column(name="date", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime date;


    @Column(name="number_of_people", nullable = false)
    private Integer numberOfPeople;

    @Column(name="comment", length = 255)
    private String comment;

    @Column(name="created_at", updatable = false,insertable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updated_at",insertable = false,updatable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt = LocalDateTime.now();


    @OneToOne(mappedBy="reservation", fetch= FetchType.LAZY)
    private Review review;

    //表示用のゲッター

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH時mm分");

    public String getFormattedDate(){
        return date.format(formatter);
    }

    public String getFormattedCreatedAt(){
        return createdAt.format(formatter);
    }
}

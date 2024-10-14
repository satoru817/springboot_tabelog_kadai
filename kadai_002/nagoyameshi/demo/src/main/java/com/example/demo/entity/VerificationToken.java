package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name="verification_tokens")
public class VerificationToken {

    @Id
    private Integer id;

    @MapsId
    @OneToOne
    @JoinColumn(name="user_id")
    private User user;

    @Column(name="token")
    private String token;

    @Column(name="created_at",insertable = false,updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

}

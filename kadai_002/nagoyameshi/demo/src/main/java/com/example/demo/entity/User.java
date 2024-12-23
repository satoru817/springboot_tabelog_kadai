package com.example.demo.entity;

import com.example.demo.validation.KatakanaOrRomanOnly;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Data
@Entity
@Table(name="users")
public class User {
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId,user.userId);
    }

    @Override
    public int hashCode(){
        return Objects.hash(userId);
    }

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

    @KatakanaOrRomanOnly
    @Column(name="name_for_reservation")//ローマ字あるいはカタカナのみを許す。
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

    @Column(name="created_at",insertable = false,updatable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user" ,fetch = FetchType.LAZY)
    private List<Card> cards;

    @OneToMany(mappedBy = "user" , fetch = FetchType.LAZY)
    private List<Subscription> subscriptions;


    @Column(name="updated_at",insertable = false,updatable = false)
    private LocalDateTime updatedAt;

    @Transient
    private String icon;//base64のimageStringを送るためのフィールド
}

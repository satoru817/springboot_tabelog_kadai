package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer reviewId;

    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    @EqualsAndHashCode.Exclude//循環参照によるstackOverFlowを防ぐ
    private Reservation reservation;

    @Column(name = "star_count", nullable = false)
    private int starCount;

    @Column(name = "content", length = 255)
    private String content;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewPhoto> photos;

    @Transient
    private List<MultipartFile> images;

}

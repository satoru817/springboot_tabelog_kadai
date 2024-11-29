package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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

    @Transient
    private Integer reservationId;

    @Column(name = "star_count", nullable = false)
    private int starCount;

    @Column(name = "content", length = 255)
    private String content;

    @OneToMany(mappedBy = "review", cascade = CascadeType.REMOVE)
    private List<ReviewPhoto> photos;

    @Transient
    private List<MultipartFile> images;

    @Column(name="is_visible" , nullable = false)
    private Boolean isVisible = true;

    @Column(name = "hidden_reason",length = 255)
    private String hiddenReason;

    @Column(name = "hidden_at")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime hiddenAt;

    @ManyToOne
    @JoinColumn(name = "hidden_by")
    private User hiddenBy;

}

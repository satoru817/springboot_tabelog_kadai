package com.example.demo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.List;

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

    @OneToMany(mappedBy="restaurant",cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reservation> reservations;

    @Transient
    private float averageStar;

    @Transient
    private Boolean isFavorite;

    //cascadeType.Allとすると、restaurantが保存、削除されるとそれに関連するRestaurantImageも自動で処理される。
    //FetchType.LAZYにすると必要なときにのみ読み込まれるようになる。
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RestaurantImage> images;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CategoryRestaurant> categoryRestaurants; // 中間テーブルとの関連を追加

    public String getOpeningHoursForDay(String day) {
        LocalTime openingTime = null;
        LocalTime closingTime = null;

        switch (day.toLowerCase()) {
            case "monday":
                openingTime = mondayOpeningTime;
                closingTime = mondayClosingTime;
                break;
            case "tuesday":
                openingTime = tuesdayOpeningTime;
                closingTime = tuesdayClosingTime;
                break;
            case "wednesday":
                openingTime = wednesdayOpeningTime;
                closingTime = wednesdayClosingTime;
                break;
            case "thursday":
                openingTime = thursdayOpeningTime;
                closingTime = thursdayClosingTime;
                break;
            case "friday":
                openingTime = fridayOpeningTime;
                closingTime = fridayClosingTime;
                break;
            case "saturday":
                openingTime = saturdayOpeningTime;
                closingTime = saturdayClosingTime;
                break;
            case "sunday":
                openingTime = sundayOpeningTime;
                closingTime = sundayClosingTime;
                break;
        }

        if (openingTime != null && closingTime != null) {
            if (openingTime.equals(closingTime)) {//営業開始と終了時刻が同じ場合は休業とする。
                return "休業";
            }
            return openingTime + " 〜 " + closingTime;
        }
        return "未設定"; // 営業時間が設定されていない場合
    }

    //fixme:日付をまたいだ営業に今の所対応できていない。

}

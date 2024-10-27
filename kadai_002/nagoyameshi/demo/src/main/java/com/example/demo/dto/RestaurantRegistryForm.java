package com.example.demo.dto;

import com.example.demo.entity.Restaurant;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;
import java.util.List;

@Data
public class RestaurantRegistryForm {
    @NotBlank(message = "レストラン名を入力してください")
    private String name;

    @NotEmpty(message = "画像を選択してください")
    private List<MultipartFile> images;

    @NotBlank(message = "説明を入力してください")
    private String description;

    @NotNull(message = "定員を入力してください")
    @Min(value = 1, message = "定員は一人以上に設定してください")
    private Integer capacity;

    @NotBlank(message = "Emailを入力してください")
    @Email(message = "メールアドレスの形式が不正です")
    private String email;

    @NotBlank(message = "電話番号を入力してください")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "電話番号の形式が不正です")
    private String phoneNumber;

    @NotBlank(message = "住所を入力してください")
    private String address;

    @NotBlank(message = "郵便番号を入力してください")
    private String postalCode;

    // 曜日ごとの開店時間と閉店時間
    private LocalTime mondayOpeningTime;
    private LocalTime mondayClosingTime;
    private LocalTime tuesdayOpeningTime;
    private LocalTime tuesdayClosingTime;
    private LocalTime wednesdayOpeningTime;
    private LocalTime wednesdayClosingTime;
    private LocalTime thursdayOpeningTime;
    private LocalTime thursdayClosingTime;
    private LocalTime fridayOpeningTime;
    private LocalTime fridayClosingTime;
    private LocalTime saturdayOpeningTime;
    private LocalTime saturdayClosingTime;
    private LocalTime sundayOpeningTime;
    private LocalTime sundayClosingTime;

    public Restaurant convertToRestaurant() {
        Restaurant restaurant = new Restaurant();
        restaurant.setAddress(this.address);
        restaurant.setName(this.name);
        restaurant.setEmail(this.email);
        restaurant.setCapacity(this.capacity);
        restaurant.setPhoneNumber(this.phoneNumber);
        restaurant.setDescription(this.description);
        restaurant.setPostalCode(this.postalCode);

        // 曜日ごとの開店時間と閉店時間を設定
        restaurant.setMondayOpeningTime(this.mondayOpeningTime);
        restaurant.setMondayClosingTime(this.mondayClosingTime);
        restaurant.setTuesdayOpeningTime(this.tuesdayOpeningTime);
        restaurant.setTuesdayClosingTime(this.tuesdayClosingTime);
        restaurant.setWednesdayOpeningTime(this.wednesdayOpeningTime);
        restaurant.setWednesdayClosingTime(this.wednesdayClosingTime);
        restaurant.setThursdayOpeningTime(this.thursdayOpeningTime);
        restaurant.setThursdayClosingTime(this.thursdayClosingTime);
        restaurant.setFridayOpeningTime(this.fridayOpeningTime);
        restaurant.setFridayClosingTime(this.fridayClosingTime);
        restaurant.setSaturdayOpeningTime(this.saturdayOpeningTime);
        restaurant.setSaturdayClosingTime(this.saturdayClosingTime);
        restaurant.setSundayOpeningTime(this.sundayOpeningTime);
        restaurant.setSundayClosingTime(this.sundayClosingTime);

        return restaurant;
    }
}

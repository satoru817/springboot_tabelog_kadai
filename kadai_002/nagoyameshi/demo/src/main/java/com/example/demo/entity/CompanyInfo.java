package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "company_info")
public class CompanyInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer companyInfoId;

    @NotBlank(message = "会社名は必須です。")
    @Size(max = 50, message = "会社名は最大50文字です。")
    @Column(nullable = false, length = 50)
    private String name;

    @Pattern(regexp = "^[0-9]{3}-[0-9]{4}$", message = "郵便番号はXXX-XXXXの形式で入力してください。")
    @Column(name = "postal_code", length = 50)
    private String postalCode;

    @Size(max = 255, message = "住所は最大255文字です。")
    @Column(length = 255)
    private String address;

    @Pattern(regexp = "^(0\\d{1,4}-\\d{1,4}-\\d{4}|\\d{10}|\\d{11})$", message = "電話番号は正しい形式で入力してください。")
    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    @Size(max = 2047, message = "説明は最大2047文字です。")
    @Column(length = 2047)
    private String description;


    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }


}

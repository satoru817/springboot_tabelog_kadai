package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "company_info")
public class CompanyInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer companyInfoId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "postal_code", length = 50)
    private String postalCode;

    @Column(length = 255)
    private String address;

    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    @Column(length = 2047)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
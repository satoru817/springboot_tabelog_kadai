package com.example.demo.dto;

import lombok.Data;

@Data
public class OpeningHours {
    private Boolean isBusinessDay;
    private String openingTime;  // "HH:mm" 形式
    private String closingTime; // "HH:mm" 形式


}

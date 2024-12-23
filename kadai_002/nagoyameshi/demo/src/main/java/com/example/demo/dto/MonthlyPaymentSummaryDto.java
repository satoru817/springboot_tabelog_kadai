package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class MonthlyPaymentSummaryDto {
    private String yearMonth;
    private Long numberOfPayments; // Changed to Long to match COUNT result type
    private Long totalEffectiveUser; // Changed to Long to match COUNT result type
}

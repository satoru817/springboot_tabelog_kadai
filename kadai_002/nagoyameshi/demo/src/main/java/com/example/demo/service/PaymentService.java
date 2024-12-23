package com.example.demo.service;

import com.example.demo.Util.UtilForString;
import com.example.demo.dto.MonthlyPaymentSummaryDto;
import com.example.demo.entity.Payment;
import com.example.demo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;


    public Page<Payment> findAllBySearchQuery(String userName, String email, LocalDate date, Pageable pageable) {
        // すべての条件が存在する場合
        if (UtilForString.isValid(userName) && UtilForString.isValid(email) && date != null) {
            return paymentRepository.findAllByNameAndEmailAndDate(userName, email, date, pageable);
        }
        // ユーザー名とメールアドレスがある場合
        else if (UtilForString.isValid(userName) && UtilForString.isValid(email)) {
            return paymentRepository.findAllByNameAndEmail(userName, email, pageable);
        }
        // ユーザー名と日付がある場合
        else if (UtilForString.isValid(userName) && date != null) {
            return paymentRepository.findAllByNameAndDate(userName, date, pageable);
        }
        // メールアドレスと日付がある場合
        else if (UtilForString.isValid(email) && date != null) {
            return paymentRepository.findAllByEmailAndDate(email, date, pageable);
        }
        // ユーザー名のみの場合
        else if (UtilForString.isValid(userName)) {
            return paymentRepository.findAllByName(userName, pageable);
        }
        // メールアドレスのみの場合
        else if (UtilForString.isValid(email)) {
            return paymentRepository.findAllByEmail(email, pageable);
        }
        // 日付のみの場合
        else if (date != null) {
            return paymentRepository.findAllByDate(date, pageable);
        }
        // 条件がない場合は全件取得
        else {
            return paymentRepository.findAll(pageable);
        }
    }

    public List<MonthlyPaymentSummaryDto> getMonthlyPaymentSummary(){
        return paymentRepository.findMonthlyPaymentSummary();
    }
}

package com.example.demo.controller;

import com.example.demo.entity.Payment;
import com.example.demo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/payment")
public class AdminPaymentController {
    private final PaymentRepository paymentRepository;

    @GetMapping("/index")
    public String index(@PageableDefault(page = 0, size=10,sort="createdAt",direction = Sort.Direction.DESC) Pageable pageable,
                        @RequestParam(name="searchQuery",required = false)String searchQuery,
                        Model model){
        Page<Payment> payments;
        if(searchQuery != null && !searchQuery.trim().isEmpty()){
            payments = paymentRepository.findAllBySearchQuery(searchQuery,pageable);
        }else{
            payments = paymentRepository.findAll(pageable);
        }

        model.addAttribute("payments",payments);

        return "admin/payment/index";

    }
}

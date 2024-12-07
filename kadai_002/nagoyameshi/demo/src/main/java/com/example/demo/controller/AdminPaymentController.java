package com.example.demo.controller;

import com.example.demo.Util.UtilForString;
import com.example.demo.entity.Payment;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.service.PaymentService;
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

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/payment")
public class AdminPaymentController {
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    @GetMapping("/index")
    public String index(@PageableDefault(page = 0, size=10,sort="createdAt",direction = Sort.Direction.DESC) Pageable pageable,
                        @RequestParam(name="userName",required = false)String userName,
                        @RequestParam(name="email",required = false)String email,
                        @RequestParam(name="paymentDate",required = false) LocalDate date,
                        Model model){

        Page<Payment> payments = paymentService.findAllBySearchQuery(userName,email,date,pageable);

        model.addAttribute("payments",payments);
        model.addAttribute("userName",userName);
        model.addAttribute("email",email);
        model.addAttribute("paymentDate",date);

        return "admin/payment/index";

    }
}

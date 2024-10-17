package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.security.UserDetailsImpl;
import com.example.demo.service.StripeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Controller
@Slf4j
public class SubscriptionController {
    private final StripeService stripeService;
    //fixme:このページはunpaid_userにしか見られないようにしないといけない。

    @GetMapping("/upgrade")
    public String upgrade(Model model){
        return "subscription/membership-upgrade";
    }

    @PostMapping("/api/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@AuthenticationPrincipal UserDetailsImpl userDetails, HttpServletRequest request) {
        User user = userDetails.getUser();
        String sessionId = stripeService.createStripeSession(user,request);
        log.info("sessionId:{}",sessionId);
        Map<String, String> response = new HashMap<>();
        response.put("sessionId", sessionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/stripe/webhook")
    public RE

}

package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.security.UserDetailsImpl;
import com.example.demo.service.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Controller
@Slf4j
public class SubscriptionController {
    private final StripeService stripeService;
    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @Value("${stripe.webhook-secret")
    private String webhookSecret;



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
    //todo:invoice.payment_failedに対応するひつようがある。（サブスクの支払い失敗への対応）
    //todo:クレジットカード情報編集ができるようにするひつようが有る。
    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> webhook(@RequestBody String payload, @RequestHeader("Stripe-Signature")String sigHeader){
        Stripe.apiKey=stripeApiKey;//暗黙的に
        Event event = null;

        try{
            event = Webhook.constructEvent(payload,sigHeader,webhookSecret);
        }catch(SignatureVerificationException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        if("checkout.session.completed".equals(event.getType())){
            stripeService.processSessionCompleted(event);
            //todo:通知メールを送る必要が有る。
        }

        return new ResponseEntity<>("Success",HttpStatus.OK);
    }

}

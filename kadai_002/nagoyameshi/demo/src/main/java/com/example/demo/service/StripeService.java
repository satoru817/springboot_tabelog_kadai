package com.example.demo.service;
import java.util.Map;
import java.util.Optional;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.param.checkout.SessionRetrieveParams;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;

import com.stripe.param.checkout.SessionCreateParams;

import jakarta.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@Slf4j
@Service
public class StripeService {
    private  final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JavaMailSender javaMailSender;
    @Value("${stripe.api-key}")//application.propertiesの設定値を流入できる
    private String stripeApiKey;
    String priceId = "price_1Q6keRRopxX1etdHX2ADJHI6";


    //セッションを作成し、Stripeに必要な情報を渡す
    public String createStripeSession(User user, HttpServletRequest httpServletRequest) {
        Stripe.apiKey = stripeApiKey;
        log.info("stripe_secret_key:{}",stripeApiKey);
        String requestUrl = new String(httpServletRequest.getRequestURL());
        String userId = user.getUserId().toString();
        log.info("createStripeSession,userId:{}",userId);
        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(priceId) // Use the Price ID for monthly subscription of 300 yen
                                .setQuantity(1L)
                                .build())
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION) // Change to subscription mode
                .setSuccessUrl(requestUrl.replaceAll("/api/create-checkout-session", "") + "/auth/success?subscription")
                .setCancelUrl(requestUrl.replaceAll("/api/create-checkout-session","/upgrade"))
                .setSubscriptionData(
                        SessionCreateParams.SubscriptionData.builder()
                                .putMetadata("userId", userId)
                                .build()
                )
                .build();

        try {
            Session session = Session.create(params);
            return session.getId();
        } catch (StripeException e) {
            log.error("Stripe API error: {}", e.getMessage());
            return "";
        }
    }



    //サブスクリプション契約完了時のメソッド
    //TODO:userのROLEを変更。さらに、確認メールを送る。
    public void processSubscriptionCreated(Event event) {
        Optional<StripeObject> optionalStripeObject = event.getDataObjectDeserializer().getObject();

        optionalStripeObject.ifPresentOrElse(stripeObject -> {
            // Check if the object is an instance of Subscription
            if (stripeObject instanceof Subscription subscription) {
                // Subscription object contains the relevant metadata
                Map<String, String> metadata = subscription.getMetadata();
                System.out.println("Metadata: " + metadata); // Log the metadata

                String retrievedUserId = metadata.get("userId");
                if (retrievedUserId == null || retrievedUserId.isEmpty()) {
                    System.out.println("Retrieved userId is null or empty.");
                    return; // Handle the error as needed
                }

                int userId;
                try {
                    userId = Integer.parseInt(retrievedUserId);
                } catch (NumberFormatException e) {
                    System.out.println("Error parsing userId: " + retrievedUserId);
                    e.printStackTrace();
                    return; // Handle the error as needed
                }

                Optional<User> optionalUser = userRepository.findById(userId);
                optionalUser.ifPresent(user -> {
                    Optional<Role> optionalPaid = roleRepository.findRoleByName("ROLE_PAID_USER");
                    optionalPaid.ifPresent(user::setRole);
                    userRepository.save(user);//upsert
                    if (optionalPaid.isPresent()) {

                        //TODO:subscription契約画面で入力したメールアドレスにも贈りたい。
                        SimpleMailMessage mailMessage = getSimpleMailMessage(user);
                        javaMailSender.send(mailMessage);
                    }
                });

            } else {
                System.out.println("Expected Subscription but got: " + stripeObject.getClass().getSimpleName());
            }

            System.out.println("ユーザーのROLE変更処理が成功しました。");
            System.out.println("Stripe API Version: " + event.getApiVersion());
            System.out.println("stripe-java Version: " + Stripe.VERSION);
        }, () -> {
            System.out.println("ユーザーのROLE変更処理が失敗しました。");
            System.out.println("Stripe API Version: " + event.getApiVersion());
            System.out.println("stripe-java Version: " + Stripe.VERSION);
        });
    }



    private static SimpleMailMessage getSimpleMailMessage(User user) {
        String recipientAddress = user.getEmail();
        String subject = "NAGOYAMESHI有料会員切り替え完了のご案内";
        String message = "あなたのアカウントは有料アカウントに切り替わりました。";

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(recipientAddress);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        return mailMessage;
    }

}


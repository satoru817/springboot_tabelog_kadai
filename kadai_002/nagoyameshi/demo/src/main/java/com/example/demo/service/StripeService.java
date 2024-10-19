package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.demo.entity.Card;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.CardRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.SubscriptionRepository;
import com.example.demo.repository.UserRepository;
import com.stripe.model.Event;
import com.stripe.model.PaymentMethod;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.param.PaymentMethodListParams;
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
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JavaMailSender javaMailSender;
    private final CardRepository cardRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentMethodService paymentMethodService;

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @Value("${stripe.price-id}")
    private String priceId;

    public String createStripeSession(User user, HttpServletRequest httpServletRequest) {
        Stripe.apiKey = stripeApiKey;
        log.info("stripe_secret_key: {}", stripeApiKey);
        String requestUrl = httpServletRequest.getRequestURL().toString();
        String userId = user.getUserId().toString();
        log.info("createStripeSession, userId: {}", userId);

        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPrice(priceId)
                        .setQuantity(1L)
                        .build())
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(requestUrl.replace("/api/create-checkout-session", "/auth/success?subscription"))
                .setCancelUrl(requestUrl.replace("/api/create-checkout-session", "/upgrade"))
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

    public void processSubscriptionCreated(Event event) {
        Optional<StripeObject> optionalStripeObject = event.getDataObjectDeserializer().getObject();

        optionalStripeObject.ifPresentOrElse(stripeObject -> {
            if (stripeObject instanceof Subscription subscription) {
                handleSubscription(subscription);
            } else {
                log.warn("Expected Subscription but received: {}", stripeObject.getClass().getSimpleName());
            }
        }, () -> log.warn("Failed to process subscription created event"));
    }

    private void handleSubscription(Subscription subscription) {
        Map<String, String> metadata = subscription.getMetadata();
        String subscriptionId = subscription.getId();
        String stripeCustomerId = subscription.getCustomer();
        log.info("Processing subscription: {}", subscriptionId);

        String retrievedUserId = metadata.get("userId");
        if (retrievedUserId == null || retrievedUserId.isEmpty()) {
            log.error("Retrieved userId is null or empty.");
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(retrievedUserId);
        } catch (NumberFormatException e) {
            log.error("Error parsing userId: {}", retrievedUserId, e);
            return;
        }

        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            log.warn("User not found with ID: {}", userId);
            return;
        }

        optionalUser.ifPresent(user -> {
            updateUserRoleAndSendMail(user, stripeCustomerId);

            // PaymentMethodServiceを使用して支払い方法を取得
            List<PaymentMethod> paymentMethods = null;
            try {
                paymentMethods = paymentMethodService.listPaymentMethods(stripeCustomerId);
            } catch (StripeException e) {
                throw new RuntimeException(e);
            }

            Card card = null; // Cardオブジェクトを初期化

            if (!paymentMethods.isEmpty()) {
                String paymentMethodId = paymentMethods.get(0).getId(); // 最初の支払い方法を取得
                card = saveCardInformation(user, paymentMethodId); // Cardを取得
            } else {
                log.warn("No payment method found for customer ID: {}", stripeCustomerId);
            }

            // カード情報が取得できた場合にSubscription情報を保存
            if (card != null) {
                saveSubscriptionInformation(subscription, user, card); // Cardを渡す
            }
        });
    }

    private Card saveCardInformation(User user, String paymentMethodId) {
        try {
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
            if (paymentMethod.getCard() != null) {
                PaymentMethod.Card card = paymentMethod.getCard();

                Card newCard = new Card();
                newCard.setUser(user);
                newCard.setStripeCardId(paymentMethod.getId());
                newCard.setBrand(card.getBrand());
                newCard.setLast4(card.getLast4());
                newCard.setExpMonth(card.getExpMonth().byteValue());
                newCard.setExpYear(card.getExpYear().shortValue());
                newCard.setIsDefault(true);

                cardRepository.save(newCard);
                log.info("Card information saved for user: {}", user.getUserId());
                return newCard; // Cardオブジェクトを返す
            }
        } catch (StripeException e) {
            log.error("Error retrieving payment method: {}", e.getMessage(), e);
        }
        return null; // 取得できなかった場合はnullを返す
    }

    private void saveSubscriptionInformation(Subscription subscription, User user, Card card) {
        com.example.demo.entity.Subscription newSubscription = new com.example.demo.entity.Subscription();
        newSubscription.setUser(user);
        newSubscription.setStripeSubscriptionId(subscription.getId());
        newSubscription.setStatus(subscription.getStatus());
        newSubscription.setStartDate(LocalDate.ofEpochDay(subscription.getStartDate()));
        newSubscription.setEndDate(LocalDate.ofEpochDay(subscription.getCurrentPeriodEnd()));
        newSubscription.setCard(card); // Cardエンティティを設定

        subscriptionRepository.save(newSubscription);
        log.info("Subscription information saved for user: {}", user.getUserId());
    }


    private void updateUserRoleAndSendMail(User user, String stripeCustomerId) {
        roleRepository.findRoleByName("ROLE_PAID_USER").ifPresent(role -> {
            user.setRole(role);
            user.setStripeCustomerId(stripeCustomerId);
            userRepository.save(user);
            SimpleMailMessage mailMessage = getSimpleMailMessage(user);
            javaMailSender.send(mailMessage);
            log.info("User role updated to paid and mail sent to user: {}", user.getEmail());
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

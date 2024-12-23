package com.example.demo.service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

import com.example.demo.entity.Card;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.CardRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.SubscriptionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.UserDetailsImpl;
import com.example.demo.security.UserDetailsServiceImpl;
import com.stripe.model.*;
import com.stripe.model.tax.Registration;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.SetupIntentCreateParams;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.model.checkout.Session;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;

import com.stripe.param.checkout.SessionCreateParams;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
    private final UserDetailsServiceImpl userDetailsService;
    private final UserService userService;
    private final SubscriptionService subscriptionService;

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @Value("${stripe.price-id}")
    private String priceId;

    @PostConstruct//@PostConstruct の「Post」は、Beanのプロパティがすべて設定された後に実行されることを示しています。
    public void init() {
        Stripe.apiKey = stripeApiKey; // APIキーを設定
    }


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
                .setSuccessUrl(requestUrl.replace(
                        "/api/create-checkout-session",
                        "/auth/success?subscription=true&session_id={CHECKOUT_SESSION_ID}"
                ))
                .setCancelUrl(requestUrl.replace("/api/create-checkout-session", "/logout"))
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

    @Transactional
    public void cancelSubscription(String subscriptionId) throws StripeException {
        log.info("cancelSubscriptionは呼びだされています。");

        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            subscription.cancel(); // 即時キャンセル
            log.info("Subscription " + subscriptionId + " has been cancelled.");
        } catch (StripeException e) {
            log.error("Failed to cancel subscription: " + subscriptionId, e);
            throw e;
        }
    }





    @Transactional
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
            updateUserRole(user,stripeCustomerId);
            sendUpgradeEmail(user);

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


        Card existingCard = cardRepository.findByStripeCardId(paymentMethodId).orElse(null);
        if (existingCard != null) {
            log.info("Existing card found for paymentMethodId: {}", paymentMethodId);
            return existingCard;
        }

        try {
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
            if (paymentMethod.getCard() != null) {
                PaymentMethod.Card card = paymentMethod.getCard();

                Card newCard = Card.builder()
                        .user(user)
                        .stripeCardId(paymentMethodId)
                        .brand(card.getBrand())
                        .last4(card.getLast4())
                        .expMonth(card.getExpMonth().byteValue())
                        .expYear(card.getExpYear().shortValue())
                        .isDefault(true)
                        .build();

                cardRepository.save(newCard);
                log.info("Card information saved for user: {}", user.getUserId());
                return newCard;
            }
        } catch (StripeException e) {
            log.error("Error retrieving payment method: {}", e.getMessage(), e);
        }
        return null;
    }

    private void saveSubscriptionInformation(Subscription subscription, User user, Card card) {
        // 既存のサブスクリプションをユーザーIDで検索
        Optional<com.example.demo.entity.Subscription> existingSubscription = subscriptionRepository.findByUser(user);

        // 新しいサブスクリプションを用意
        com.example.demo.entity.Subscription newSubscription;

        if (existingSubscription.isPresent()) {
            // 既存のサブスクリプションが存在する場合はそのインスタンスを取得して更新
            newSubscription = existingSubscription.get();
        } else {
            // 存在しない場合は新しいインスタンスを作成
            newSubscription = new com.example.demo.entity.Subscription();
            newSubscription.setUser(user);
        }

        // サブスクリプションの情報を設定
        newSubscription.setStripeSubscriptionId(subscription.getId());
        newSubscription.setStatus(subscription.getStatus());
        newSubscription.setStartDate(Instant.ofEpochSecond(subscription.getStartDate())
                .atZone(ZoneId.systemDefault())
                .toLocalDate());
        newSubscription.setEndDate(Instant.ofEpochSecond(subscription.getCurrentPeriodEnd())
                .atZone(ZoneId.systemDefault())
                .toLocalDate());
        newSubscription.setCard(card);

        // サブスクリプションを保存（新規作成または更新）
        subscriptionRepository.save(newSubscription);
        log.info("Subscription information saved for user: {}", user.getUserId());
    }



    @Transactional
    public void processSubscriptionDeleted(Event event) throws StripeException {
        // イベントデータからサブスクリプションIDを取得
        Subscription subscription = (Subscription) event.getData().getObject();
        String subscriptionId = subscription.getId();

        //ストライプ上でユーザーからカードを切り離す
        detachCardInformationInStripe(subscription);

        // データベースから該当のサブスクリプションを取得
        com.example.demo.entity.Subscription dbSubscription = subscriptionService.findByStripeSubscriptionId(subscriptionId);

        // サブスクリプションを「キャンセル」状態に更新
        dbSubscription.setStatus("canceled");
        subscriptionRepository.save(dbSubscription);

        // ユーザーのroleの変更処理
        User user = dbSubscription.getUser();

        cardRepository.deleteByUser(user);

    }

    private void detachCardInformationInStripe(Subscription subscription) throws StripeException {

        String customerId = subscription.getCustomer();

        // 顧客のカード情報を取得
        Map<String, Object> params = new HashMap<>();
        params.put("customer", customerId);
        params.put("type", "card");

        PaymentMethodCollection paymentMethods = PaymentMethod.list(params);

        // カード情報のdetach
        for (PaymentMethod paymentMethod : paymentMethods.getData()) {
            try {
                paymentMethod.detach();
                log.info("Detached card: " + paymentMethod.getId());
            } catch (StripeException e) {
                log.error("Failed to detach card: " + paymentMethod.getId(), e);
            }
        }
    }


    // このメソッドはユーザー情報の更新のみを担当

    private void updateUserRole(User user, String stripeCustomerId) {
        roleRepository.findRoleByName("ROLE_PAID_USER").ifPresent(role -> {
            user.setRole(role);
            user.setStripeCustomerId(stripeCustomerId);
            userRepository.save(user);
        });
    }

    // メール送信は別メソッドに分離
    private void sendUpgradeEmail(User user) {
        try {
            SimpleMailMessage mailMessage = getSimpleMailMessage(user);
            javaMailSender.send(mailMessage);
            log.info("Mail sent to user: {}", user.getEmail());
        } catch (Exception e) {
            // メール送信エラーをログに記録するだけで、トランザクションには影響を与えない
            log.error("Failed to send email to user: {}", user.getEmail(), e);
        }
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


    //セットアップインテントを作成するためのサービスクラス
    public String createSetupIntent(String customerId) throws StripeException {
        SetupIntentCreateParams params = SetupIntentCreateParams.builder()
                .setCustomer(customerId)
                .build();

        SetupIntent setupIntent = SetupIntent.create(params);
        return setupIntent.getClientSecret(); // クライアントシークレットを返す
    }

    //新たなクレジットカード情報を顧客に結びつけるメソッド
    public void attachPaymentMethodToCustomer(String paymentMethodId, String customerId) throws StripeException {
        // PaymentMethodを顧客に追加する
        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
        PaymentMethodAttachParams params = PaymentMethodAttachParams.builder()
                .setCustomer(customerId)
                .build();
        paymentMethod.attach(params);
    }

    public void savePaymentMethodToDatabase(PaymentMethod paymentMethod,String stripeCustomerId) {
        // 必要な情報を取り出す
        String brand = paymentMethod.getCard().getBrand();
        String last4 = paymentMethod.getCard().getLast4();
        Long expMonth = paymentMethod.getCard().getExpMonth();
        Long expYear = paymentMethod.getCard().getExpYear();
        String stripeCardId = paymentMethod.getId();

        // stripeCustomerId でユーザーを検索
        Optional<User> userOptional = userRepository.findByStripeCustomerId(stripeCustomerId);

        // ユーザーが存在する場合のみ処理を進める
        userOptional.ifPresent(user -> {
            // Cardエンティティを作成し、情報をセット
            log.info("userは存在します:{}",user.getName());

            Card card = Card.builder()
                        .stripeCardId(stripeCardId)
                        .brand(brand)
                        .user(user)
                        .last4(last4)
                        .expMonth(expMonth.byteValue())
                        .expYear(expYear.shortValue())
                        .isDefault(false)
                        .build();

            // カード情報をデータベースに保存
            cardRepository.save(card);
        });
    }

}

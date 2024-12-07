package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PaymentMethodRequest;
import com.example.demo.entity.*;
import com.example.demo.entity.Card;
import com.example.demo.entity.Subscription;
import com.example.demo.repository.*;
import com.example.demo.security.UserDetailsImpl;
import com.example.demo.security.UserDetailsServiceImpl;
import com.example.demo.service.CardService;
import com.example.demo.service.StripeService;
import com.example.demo.service.SubscriptionService;
import com.example.demo.service.UserService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentMethodAttachParams;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;

@RequiredArgsConstructor
@Controller
@Slf4j
public class SubscriptionController {
    private final StripeService stripeService;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final RoleRepository roleRepository;
    private final PaymentRepository paymentRepository;
    private final CardService cardService;
    private final UserService userService;
    private final SubscriptionService subscriptionService;
    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;


    //fixme:このページはunpaid_userにしか見られないようにしないといけない。
    //todo:authenticationの内容を編集する。
    @GetMapping("/upgrade")
    public String upgrade(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails, HttpServletRequest request) {
        // セッションの取得
        userDetailsService.updateUserRolesAndSession(userDetails, request);

        // ユーザー名から最新のユーザー情報を取得
        User user = userRepository.findByName(userDetails.getUsername()).orElse(null);
        if (user != null) {
            Optional<Subscription> optionalSubscription = subscriptionRepository.findByUser(user);
            optionalSubscription.ifPresent(subscription -> {
                Boolean ifUserHasCancelledSubscription = subscription.getStatus().equals("cancelled");
                model.addAttribute("ifUserHasCancelledSubscription", ifUserHasCancelledSubscription);
            });
        }

        return "subscription/membership-upgrade";
    }


    @PostMapping("/api/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@AuthenticationPrincipal UserDetailsImpl userDetails, HttpServletRequest request) {
        User user = userDetails.getUser();
        String sessionId = stripeService.createStripeSession(user, request);
        log.info("sessionId:{}", sessionId);
        Map<String, String> response = new HashMap<>();
        response.put("sessionId", sessionId);
        return ResponseEntity.ok(response);
    }


    //サブスクをキャンセルするが、カード情報はstripeから消さない。これは再度同一カードでサブスクを開始したいときの入力を省略するため。
    @Transactional
    @PostMapping("/api/cancel-subscription")
    public ResponseEntity<Map<String, Object>> cancelSubscription(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request) throws StripeException {

        User user = userDetails.getUser();
        Optional<Subscription> optionalSubscription = subscriptionRepository.findByUser(user);

        if (optionalSubscription.isEmpty()) {
            // サブスクリプションが見つからない場合はエラーレスポンスを返す
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "No active subscription found.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        Subscription subscription = optionalSubscription.get();
        String subscriptionId = subscription.getStripeSubscriptionId();
        log.info("Stripe APIキー: {}", stripeApiKey);
        log.info("stripe サブスクリプションId:{}", subscriptionId);

        try {

            stripeService.cancelSubscription(subscriptionId);

            cardService.setAllNonDefaultForUser(user);

            userService.togglePaymentStatus(user);


            // 成功メッセージを作成
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("success", true);
            successResponse.put("message", "Subscription has been canceled.");
            log.info("サクセスメッセージを格納しました。");
            return ResponseEntity.ok(successResponse);

        } catch (StripeException e) {
            // Stripe関連のエラーが発生した場合の処理
            log.error("Stripeエラーが発生しました: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to cancel subscription. Please try again.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    //todo:invoice.payment_failedに対応するひつようがある。（サブスクの支払い失敗への対応）
    //todo:クレジットカード情報編集ができるようにするひつようが有る。
    @Transactional
    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> webhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) throws StripeException {

        log.info("webhookイベントを受け取っています。");
        Stripe.apiKey = stripeApiKey; // Stripe APIキーを設定
        log.info("stripeApiKey: {}", stripeApiKey);
        log.info("sigHeader: {}", sigHeader);
        log.info("webhookSecret: {}", webhookSecret);
        Event event = null;

        try {
            log.info("webhookイベントを作ろうとしています");
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.info("webhookイベントは作ることができませんでした。");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        log.info("イベントのタイプは: {}", event.getType());

        // 新規サブスクリプション作成時の処理
        if ("customer.subscription.created".equals(event.getType())) {
            stripeService.processSubscriptionCreated(event);
            log.info("stripeService.processSubscriptionCreatedは呼びだされました。");
            // TODO: cards, subscriptions, usersの処理
        }

        // サブスクリプションキャンセル時の処理
        if ("customer.subscription.deleted".equals(event.getType())) {
            stripeService.processSubscriptionDeleted(event);
            log.info("サブスクリプションがキャンセルされました。");

        }

        if ("setup_intent.succeeded".equals(event.getType())) {
            log.info("setup_intent.succeededが呼びだされています");
            SetupIntent setupIntent = (SetupIntent) event.getDataObjectDeserializer().getObject().orElse(null);
            if (setupIntent != null) {
                String paymentMethodId = setupIntent.getPaymentMethod();
                String customerId = setupIntent.getCustomer();
                log.info("stripeCustomerIdをsetUpIntentから回収しました:{}", customerId);
                try {
                    // paymentMethodId から PaymentMethod オブジェクトを取得
                    PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
                    log.info("paymentMethodの獲得ができました.{}", paymentMethod.toJson());

                    // Stripeサービスでカード情報の保存処理を実行
                    stripeService.savePaymentMethodToDatabase(paymentMethod, customerId);
                    log.info("カードが正常に登録されました。PaymentMethod ID: {}", paymentMethodId);
                } catch (StripeException e) {
                    log.error("PaymentMethodの取得に失敗しました。PaymentMethod ID: {}", paymentMethodId, e);
                }
            } else {
                log.warn("SetupIntentが取得できませんでした。");
            }
        }

        //支払いがされたときに記録しないといけない。
        if ("invoice.paid".equals(event.getType())) {
            log.info("invoice.paidが呼び出されています");
            Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);

            if (invoice != null) {
                String stripePaymentIntentId = invoice.getPaymentIntent();
                //べき等性チェック
                if (paymentRepository.existsByStripePaymentIntentId(stripePaymentIntentId)) {
                    log.error("すでに登録されている支払いです");
                } else {

                    try {
                        // Stripeの顧客IDからユーザーを特定
                        Integer userId = Integer.valueOf(invoice.getSubscriptionDetails().getMetadata().get("userId"));
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません: " + userId));

                        // 支払い方法の情報を取得
                        PaymentIntent paymentIntent = PaymentIntent.retrieve(stripePaymentIntentId);
                        String paymentMethodId = paymentIntent.getPaymentMethod();

                        // カード情報を取得または作成
                        Card card = cardRepository.findByStripeCardId(paymentMethodId)
                                .orElseGet(() -> {
                                    try {
                                        Card currentDefault = cardRepository.findByUserAndIsDefaultTrue(user)
                                                .orElse(null);

                                        if (currentDefault != null) {
                                            currentDefault.setIsDefault(false);
                                            cardRepository.save(currentDefault);
                                        }

                                        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
                                        PaymentMethod.Card pmCard = paymentMethod.getCard();

                                        Card newCard = Card.builder()
                                                .user(user)
                                                .stripeCardId(paymentMethodId)
                                                .brand(pmCard.getBrand())
                                                .last4(pmCard.getLast4())
                                                .expMonth(pmCard.getExpMonth().byteValue())
                                                .expYear(pmCard.getExpYear().shortValue())
                                                .isDefault(true)
                                                .build();
                                        Card s = cardRepository.save(newCard);
                                        cardRepository.flush();
                                        return s;
                                    } catch (StripeException e) {
                                        log.error("カード情報の取得に失敗しました: {}", paymentMethodId, e);
                                        return null;
                                    }
                                });

                        // 支払い情報を作成
                        Payment payment = Payment.builder()
                                .user(user)
                                .amount(invoice.getAmountPaid().intValue())
                                .currency(invoice.getCurrency().toUpperCase())
                                .stripePaymentIntentId(stripePaymentIntentId)
                                .card(card)
                                .status(PaymentStatus.SUCCEEDED)
                                .description(invoice.getDescription())
                                .metadata(invoice.getMetadata().toString())
                                .build();

                        // 保存
                        paymentRepository.save(payment);
                        log.info("支払い情報を保存しました: {}", stripePaymentIntentId);

                    } catch (Exception e) {
                        log.error("支払い情報の保存に失敗しました: {}", stripePaymentIntentId, e);
                        throw e;
                    }
                }
            }
        }


        return new ResponseEntity<>("Success", HttpStatus.OK);
    }


    @PostMapping("api/create-setup-intent")
    public ResponseEntity<String> createSetupIntent(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        String customerId = userDetails.getUser().getStripeCustomerId();
        try {
            String clientSecret = stripeService.createSetupIntent(customerId);
            log.info("clientSecret:{}", clientSecret);
            return ResponseEntity.ok(clientSecret); // クライアントシークレットをレスポンスとして返す
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("api/add-payment-method")
    public ResponseEntity<?> addPaymentMethod(
            @RequestBody PaymentMethodRequest paymentMethodRequest,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws StripeException {
        log.info("api/add-payment-methodは呼び出されました。");

        if (userDetails == null) {
            log.warn("User is not authenticated.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "User is not authenticated."));
        }

        // ログインしているユーザーから顧客IDを取得
        String customerId = userDetails.getUser().getStripeCustomerId();
        log.info("customerId: {}", customerId); // ここでnullか確認

        if (customerId == null) {
            log.warn("Stripe customer ID is null for user: {}", userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "Stripe customer ID is not available."));
        }

        // 受け取ったpaymentMethodIdを顧客に追加
        stripeService.attachPaymentMethodToCustomer(paymentMethodRequest.getPaymentMethodId(), customerId);

        // 成功レスポンスを返す
        return ResponseEntity.ok().body(new ApiResponse(true, "Payment method added successfully!"));
    }


    @GetMapping("/add_card")
    public String addCard() {
        return "/subscription/add_card";
    }

    @GetMapping("/cardView")
    @Transactional
    public String viewUserCards(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        User user = userDetails.getUser();
        List<Card> cards = cardRepository.getByUser(user);
        model.addAttribute("cards", cards);
        return "subscription/card_view";
    }

    @PostMapping("/cards/delete/{id}")
    public String deleteCard(@PathVariable("id") Integer cardId, RedirectAttributes redirectAttributes) {

        Card card = cardService.findById(cardId);

        String stripeCardId = card.getStripeCardId();

        String stripeCustomerId = card.getUser().getStripeCustomerId();

        // Step 2: Stripeからカード情報を消去
        try {
            // ストライプ上で顧客から支払い方法を取得
            PaymentMethod paymentMethod = PaymentMethod.retrieve(stripeCardId);
            if (paymentMethod != null) {
                // 顧客から支払い方法を切り離す
                paymentMethod.detach();
                log.info("Successfully detached card from Stripe: {}", stripeCardId);

            }
        } catch (StripeException e) {
            log.error("Failed to delete card from Stripe: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete card from Stripe.");
            return "redirect:/cards";
        }

        // Step 3: DBからカードを削除
        cardRepository.deleteById(cardId);
        log.info("Card deleted from the database: {}", cardId);//ここでsubscriptionもdeleteされてしまっている？

        redirectAttributes.addFlashAttribute("successMessage", "Card successfully deleted.");

        return "redirect:/cardView"; // カードページにリダイレクト
    }

    @Transactional
    @PostMapping("/cards/set-default/{id}")
    public String changeDefaultCard(@PathVariable("id") Integer cardId, RedirectAttributes redirectAttributes, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // ユーザーを取得
        User user = userDetails.getUser();// ユーザーの取得

        // ユーザーのデフォルトカードのみを探す
        List<Card> cards = cardRepository.findByUserAndIsDefault(user, true);

        //すべてのカードをデフォルトでなくす
        for (Card card : cards) {
            card.setIsDefault(false);
            cardRepository.save(card);
            cardRepository.flush();// DBを更新

        }

        // 指定されたカードをデフォルトに設定
        Card toDefaultCard = cardService.findById(cardId);

        toDefaultCard.setIsDefault(true);

        cardRepository.save(toDefaultCard);

        Subscription subscription = subscriptionService.findByUser(user);

        subscription.setCard(toDefaultCard);

        subscriptionRepository.save(subscription);

        try {
            // Stripeでデフォルトカードを顧客に設定する
            Map<String, Object> params = new HashMap<>();
            params.put("invoice_settings", Collections.singletonMap("default_payment_method", toDefaultCard.getStripeCardId()));

            Customer customer = Customer.retrieve(toDefaultCard.getUser().getStripeCustomerId());
            customer.update(params);

            log.info("顧客のデフォルト支払い方法がStripeで更新されました。");
        } catch (StripeException e) {
            log.error("Stripeの顧客情報を更新できませんでした: {}", e.getMessage());
        }


        redirectAttributes.addFlashAttribute("successMessage", "カードがデフォルトに設定されました。");


        return "redirect:/cardView"; // カード一覧ページにリダイレクト
    }


}

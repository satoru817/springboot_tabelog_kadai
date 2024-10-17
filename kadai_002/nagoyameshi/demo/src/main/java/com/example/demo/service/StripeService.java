package com.example.demo.service;
import java.util.Map;
import java.util.Optional;
import com.example.demo.entity.User;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.param.checkout.SessionRetrieveParams;
import com.stripe.model.checkout.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;

import com.stripe.param.checkout.SessionCreateParams;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Service
public class StripeService {
    @Value("${stripe.api-key}")//application.propertiesの設定値を流入できる
    private String stripeApiKey;
    String priceId = "price_1Q6keRRopxX1etdHX2ADJHI6";


    //セッションを作成し、Stripeに必要な情報を渡す
    public String createStripeSession(User user, HttpServletRequest httpServletRequest) {
        Stripe.apiKey = stripeApiKey;
        log.info("stripe_secret_key:{}",stripeApiKey);
        String requestUrl = new String(httpServletRequest.getRequestURL());
        String userId = String.valueOf(user.getUserId());
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


//    //セッションから予約情報を取得。ReservationServiceクラスを介してデータベースに登録
//    public void processSessionCompleted(Event event) {
//        Optional<StripeObject> optionalStripeObject = event.getDataObjectDeserializer().getObject();
//        optionalStripeObject.ifPresentOrElse(stripeObject ->{
//                    Session session = (Session) stripeObject;
//                    SessionRetrieveParams params = SessionRetrieveParams.builder().addExpand("payment_intent").build();
//
//                    try {
//                        session = Session.retrieve(session.getId(),params,null);
//                        Map<String,String> paymentIntentObject = session.getPaymentIntentObject().getMetadata();
//                        reservationService.create(paymentIntentObject);
//                    }catch(StripeException e) {
//                        e.printStackTrace();
//                    }
//                    System.out.println("予約一覧ページの登録処理が成功しました。");
//                    System.out.println("Stripe API Version:"+event.getApiVersion());
//                    System.out.println("stripe-java Version:"+Stripe.VERSION);
//                },
//                ()->{
//                    System.out.println("予約一覧ページの登録処理が失敗しました。");
//                    System.out.println("Stripe API Version:"+event.getApiVersion());
//                    System.out.println("stripe-java Version:"+Stripe.VERSION);
//                });
//    }

}


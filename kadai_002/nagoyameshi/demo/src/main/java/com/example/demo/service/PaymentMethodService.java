package com.example.demo.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentMethod;
import com.stripe.param.PaymentMethodListParams;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentMethodService {

    // Stripeの顧客IDを受け取って支払い方法をリストするメソッド
    public List<PaymentMethod> listPaymentMethods(String customerId) throws StripeException {
        PaymentMethodListParams params = PaymentMethodListParams.builder()
                .setCustomer(customerId)
                .setType(PaymentMethodListParams.Type.CARD) // 必要に応じてタイプを指定
                .build();

        return PaymentMethod.list(params).getData();
    }
}

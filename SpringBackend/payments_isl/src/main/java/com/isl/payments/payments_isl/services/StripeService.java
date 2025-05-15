package com.isl.payments.payments_isl.services;

import com.isl.payments.payments_isl.DTO.ProductRequest;
import com.isl.payments.payments_isl.entities.Payment;
import com.isl.payments.payments_isl.DTO.ProductResponse;
import com.isl.payments.payments_isl.repositories.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    @Value("${stripe.secretKey}")
    private String secretKey;

    @Autowired
    PaymentRepository paymentRepository;

    @Transactional
    public ProductResponse createPayment(ProductRequest productRequest) throws StripeException {
        Stripe.apiKey=secretKey;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("amount", productRequest.getAmount());
            params.put("currency", productRequest.getCurrency());
            params.put("payment_method_types", Arrays.asList("card"));

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            Payment payment = new Payment();
            payment.setAmount(productRequest.getAmount());
            payment.setPaymentStatus("PENDING");
            payment.setUsername(productRequest.getUsername());
            payment.setCurrency(productRequest.getCurrency());
            payment.setOrderId(productRequest.getOrderId());
            payment.setTransactionId(paymentIntent.getId());
            payment.setUsername(productRequest.getUsername());
            payment.setCreatedAt(LocalDateTime.now());

            paymentRepository.save(payment);
            return ProductResponse.builder()
                    .status("SUCCESS")
                    .message("Payment Intent created successfully")
                    .clientSecret(paymentIntent.getClientSecret()) // Send to frontend for confirmation
                    .build();
        }
        catch (StripeException e) {
            e.printStackTrace();
            return ProductResponse.builder()
                    .status("FAILED")
                    .message(e.getMessage())
                    .build();
        }
    }
}

package com.isl.payments.payments_isl.controllers;

import com.isl.payments.payments_isl.entities.Payment;
import com.isl.payments.payments_isl.repositories.PaymentRepository;
import com.isl.payments.payments_isl.security.JwtHelper;
import com.isl.payments.payments_isl.services.UserClient;
import com.isl.payments.payments_isl.services.UserService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stripe")
public class PaymentWebhookController {

    private final PaymentRepository paymentRepository;

    public PaymentWebhookController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Autowired
    UserClient userClient;

    @Autowired
    JwtHelper jwtHelper;

    @Autowired
    UserService userService;

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody String payload,
                                           @RequestHeader("Stripe-Signature") String signature

                                            ) {



        try {
            String endpointSecret = "whsec_605c61b2f0a968f261968f6a78adf8bb6b4b29490ed4cdb364bc1ebd99d4403f"; // ✅ Store this securely
            Event event = Webhook.constructEvent(payload, signature, endpointSecret);

            if ("payment_intent.succeeded".equals(event.getType())) {
                String paymentIntentId = ((PaymentIntent) event.getData().getObject()).getId();

                Payment payment = paymentRepository.findByTransactionId(paymentIntentId);
                if (payment != null) {
                    payment.setPaymentStatus("SUCCESS");
                    paymentRepository.save(payment);
                    userService.updateUser(payment.getUsername());

                }
            } else if ("payment_intent.payment_failed".equals(event.getType())) {
                String paymentIntentId = ((PaymentIntent) event.getData().getObject()).getId();

                Payment payment = paymentRepository.findByTransactionId(paymentIntentId);
                if (payment != null) {
                    payment.setPaymentStatus("FAILED");
                    paymentRepository.save(payment);
                }
            }

            return ResponseEntity.ok("Webhook handled");

        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error");
        }
    }
}
